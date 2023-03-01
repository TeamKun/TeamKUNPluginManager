package org.kunlab.kpm.resolver.impl.github;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.http.HTTPResponse;
import org.kunlab.kpm.http.RequestContext;
import org.kunlab.kpm.http.Requests;
import org.kunlab.kpm.interfaces.resolver.URLResolver;
import org.kunlab.kpm.interfaces.resolver.result.ErrorResult;
import org.kunlab.kpm.interfaces.resolver.result.MultiResult;
import org.kunlab.kpm.interfaces.resolver.result.ResolveResult;
import org.kunlab.kpm.resolver.ErrorCause;
import org.kunlab.kpm.resolver.QueryContext;
import org.kunlab.kpm.resolver.impl.GitHubSuccessResult;
import org.kunlab.kpm.resolver.result.ErrorResultImpl;
import org.kunlab.kpm.resolver.result.MultiResultImpl;
import org.kunlab.kpm.resolver.utils.URLResolveUtil;
import org.kunlab.kpm.versioning.Version;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitHubURLResolver implements URLResolver
{
    private static final String GITHUB_REPO_RELEASES_URL = "https://api.github.com/repos/%s/releases";
    private static final String GITHUB_REPO_RELEASE_NAME_URL = GITHUB_REPO_RELEASES_URL + "/tags/%s";

    private static boolean endsWithIgn(String str, String suffix)
    {
        return StringUtils.endsWithIgnoreCase(str, suffix);
    }

    private static boolean endsWithIgn(String str, String suffix, String suffix2)
    {
        return endsWithIgn(str, suffix) || endsWithIgn(str, suffix2);
    }

    private static long calcReputation(GitHubSuccessResult result)
    {
        long reputation = 0;

        String fileName = result.getFileName();
        String version = result.getVersion();
        boolean isPreRelease = result.isPreRelease();
        assert fileName != null;
        assert version != null;

        Version versionObj;
        if (Version.isValidVersionString(version))
            versionObj = Version.of(version);
        else
            versionObj = Version.of("0.0.0");

        reputation += versionObj.getMajor().getIntValue() * 1000L;
        reputation += versionObj.getMinor().getIntValue() * 100L;
        reputation += versionObj.getPatch().getIntValue() * 10L;
        if (versionObj.getPreRelease() != null)
            reputation -= versionObj.getPreRelease().getRawValue().chars().sum();

        if (endsWithIgn(fileName, ".jar", ".zip"))
            reputation += 1000L;

        if (endsWithIgn(fileName, ".plugin.jar", ".plugin.zip"))
            reputation += 500L;
        else if (endsWithIgn(fileName, ".api.jar", ".api.zip"))
            reputation -= 5000L;

        if (isPreRelease && !versionObj.isPreRelease())
            // isPreRelease is given by GitHub API, but versionObj.isPreRelease() is contained in version string
            reputation -= 500L;

        return reputation;
    }

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        GHURLParseResult parsedURL;
        try
        {
            parsedURL = GHURLParser.parse(query.getQuery());
        }
        catch (IllegalArgumentException ex)
        {
            return new ErrorResultImpl(
                    this,
                    ErrorCause.INVALID_QUERY,
                    ResolveResult.Source.GITHUB
            );
        }

        if (parsedURL.getFinalName() != null)
            // Return because of the query is directly linking to the plugin file.
            return new GitHubSuccessResult(
                    this,
                    query.getQuery(),
                    parsedURL.getFinalName(),
                    parsedURL.getTag(),
                    parsedURL.getRepositoryName(),
                    parsedURL.getOwner(),
                    -1,
                    "Provided by Static URL Context Parsing",
                    "Provided by Static URL Context Parsing",
                    -1,
                    false
            );

        return this.processGitHubAPI(parsedURL, query.getVersion());
    }

    @Override
    public ResolveResult autoPickOnePlugin(MultiResult multiResult)
    {
        HashMap<Long, ResolveResult> map = new HashMap<>();
        ErrorResult firstError = null;
        for (ResolveResult result : multiResult.getResults())
        {
            if (result instanceof GitHubSuccessResult)
            {
                GitHubSuccessResult successResult = (GitHubSuccessResult) result;
                map.put(calcReputation(successResult), successResult);
            }
            else if (result instanceof ErrorResult && firstError == null)
                firstError = (ErrorResult) result;
        }

        if (map.isEmpty())
            return firstError;

        return map.entrySet().stream().parallel()
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    private ResolveResult processGitHubAPI(GHURLParseResult parsedURL, @Nullable Version version)
    {
        String owner = parsedURL.getOwner();
        String repositoryName = parsedURL.getRepositoryName();
        String repository = parsedURL.getRepository();
        String tag = parsedURL.getTag();

        String apiURL;
        if (tag != null)  // Use different API URL to specify the release by tag name
            apiURL = String.format(GITHUB_REPO_RELEASE_NAME_URL, repository, tag);
        else
            apiURL = String.format(GITHUB_REPO_RELEASES_URL, repository);

        HTTPResponse response = Requests.request(RequestContext.builder()
                .url(apiURL)
                .build());

        ErrorResult mayError = URLResolveUtil.processErrorResponse(this, response, ResolveResult.Source.GITHUB);

        if (mayError != null)
            return mayError;

        if (tag != null) // When tag is specified, the response is a json object of the release, not an array.
        {
            JsonObject jsonObject = response.getAsJson().getAsJsonObject();

            return this.buildResultSingle(owner, repositoryName, jsonObject, version);
        }

        JsonArray jsonArray = response.getAsJson().getAsJsonArray();
        List<ResolveResult> results = new ArrayList<>();

        boolean isFound = false;
        boolean isNoAssets = false;
        for (JsonElement jsonElement : jsonArray)
        {
            ResolveResult result = this.buildResultSingle(owner, repositoryName, jsonElement.getAsJsonObject(), version);

            if (result instanceof ErrorResult)
            {
                ErrorResult errorResult = (ErrorResult) result;
                if (errorResult.getCause() == ErrorCause.VERSION_MISMATCH)
                    isFound = true;
                else if (errorResult.getCause() == ErrorCause.ASSET_NOT_FOUND)
                    isNoAssets = true;
                continue;
            }
            else if (version != null)
                return result; // If the version is specified, return the result because it is the only result(version is must be equal).

            results.add(result);
        }

        if (results.isEmpty())
            if (isFound)
                return new ErrorResultImpl(
                        this,
                        ErrorCause.VERSION_MISMATCH,
                        ResolveResult.Source.GITHUB
                );
            else if (isNoAssets)
                return new ErrorResultImpl(
                        this,
                        ErrorCause.ASSET_NOT_FOUND,
                        ResolveResult.Source.GITHUB
                );


        return new MultiResultImpl(this, results.toArray(new ResolveResult[0]));
    }

    private ResolveResult buildResultSingle(String owner, String repositoryName, JsonObject object, @Nullable Version queryVersion)
    {
        List<GitHubSuccessResult> results = new ArrayList<>();

        String releaseName = object.get("name").getAsString();
        String body = object.get("body").getAsString();
        String version = object.get("tag_name").getAsString();
        long releaseId = object.get("id").getAsLong();

        if (queryVersion != null && !(Version.isValidVersionString(version) &&
                Version.of(version).isEqualTo(queryVersion)))
            return new ErrorResultImpl(
                    this,
                    ErrorCause.VERSION_MISMATCH,
                    ResolveResult.Source.GITHUB
            );

        JsonArray assets = object.getAsJsonArray("assets");

        if (assets.size() == 0)
            return new ErrorResultImpl(
                    this,
                    ErrorCause.ASSET_NOT_FOUND,
                    ResolveResult.Source.GITHUB
            );

        for (JsonElement asset : assets)
        {
            if (!asset.isJsonObject())
                continue;

            JsonObject assetObject = asset.getAsJsonObject();

            String downloadURL = assetObject.get("url").getAsString();
            String fileName = assetObject.get("name").getAsString();
            long size = assetObject.get("size").getAsLong();
            boolean isPreRelease = object.get("prerelease").getAsBoolean();

            GitHubSuccessResult result = new GitHubSuccessResult(this, downloadURL, fileName, version, repositoryName, owner, size, releaseName, body, releaseId, isPreRelease);

            if (assets.size() == 1)
                return result;
            else
                results.add(result);
        }

        if (results.size() == 1)
            return results.get(0);

        return new MultiResultImpl(this, results.toArray(new GitHubSuccessResult[0]));
    }

    @Override
    public String[] getHosts()
    {
        return new String[]{"github.com", "www.github.com"};
    }
}
