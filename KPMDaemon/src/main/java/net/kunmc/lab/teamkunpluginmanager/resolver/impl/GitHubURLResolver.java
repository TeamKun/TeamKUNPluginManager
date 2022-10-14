package net.kunmc.lab.teamkunpluginmanager.resolver.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kunmc.lab.teamkunpluginmanager.resolver.QueryContext;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.URLResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult;
import net.kunmc.lab.teamkunpluginmanager.utils.http.HTTPResponse;
import net.kunmc.lab.teamkunpluginmanager.utils.http.RequestContext;
import net.kunmc.lab.teamkunpluginmanager.utils.http.Requests;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubURLResolver implements URLResolver
{
    private static final String GITHUB_REPO_RELEASES_URL = "https://api.github.com/repos/%s/releases";
    private static final String GITHUB_REPO_RELEASE_NAME_URL = GITHUB_REPO_RELEASES_URL + "/tags/%s";

    private static final Pattern GITHUB_REPO_PATTERN = Pattern.compile("^/(?<repository>(?<owner>[a-zA-Z\\d]" +
            "(?:[a-zA-Z\\d]|-(?=[a-zA-Z\\d])){0,38})/(?<repoName>\\w(?:\\w|-(?=\\w)){0,100}))" +
            "(?:/(?:tags|releases(?:/(?:tag/(?<tag>[^/]+)/?$|download/(?<downloadTag>[^/]+)/" +
            "(?<fileName>[^/]+)))?))?/?$");

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        Matcher matcher = this.urlMatcher(GITHUB_REPO_PATTERN, query.getQuery());

        if (matcher == null)
            return new ErrorResult(this, ErrorResult.ErrorCause.INVALID_QUERY, ResolveResult.Source.GITHUB);

        String repository = null;
        String owner = null;
        String tag = null;
        String repositoryName = null;

        while (matcher.find())
        {
            String repositoryGroup = matcher.group("repository");
            String downloadTagGroup = matcher.group("downloadTag");
            String tagGroup = matcher.group("tag");
            String fileNameGroup = matcher.group("fileName");
            String ownerGroup = matcher.group("owner");
            String repoNameGroup = matcher.group("repoName");

            if (fileNameGroup != null && !fileNameGroup.isEmpty()) // URLが自己解決。
                return new SuccessResult(this, query.getQuery(), ResolveResult.Source.GITHUB);

            if (!repositoryGroup.isEmpty())
                repository = repositoryGroup;

            if (downloadTagGroup != null && !downloadTagGroup.isEmpty())
                tag = downloadTagGroup;
            else if (tagGroup != null && !tagGroup.isEmpty())
                tag = tagGroup;

            if (ownerGroup != null && !ownerGroup.isEmpty())
                owner = ownerGroup;

            if (repoNameGroup != null && !repoNameGroup.isEmpty())
                repositoryName = repoNameGroup;
        }

        if (repository == null)
            return new ErrorResult(this, ErrorResult.ErrorCause.INVALID_QUERY, ResolveResult.Source.GITHUB);

        return this.processGitHubAPI(owner, repositoryName, repository, tag, query.getVersion());
    }

    @Override
    public ResolveResult autoPickOnePlugin(MultiResult multiResult)
    {
        return this.autoPickFirst(multiResult, ResolveResult.Source.GITHUB);
    }

    private ResolveResult processGitHubAPI(String owner, String repositoryName, String repository, String tag, @Nullable String version)
    {
        String apiURL;
        if (tag != null)
            apiURL = String.format(GITHUB_REPO_RELEASE_NAME_URL, repository, tag);
        else
            apiURL = String.format(GITHUB_REPO_RELEASES_URL, repository);

        HTTPResponse response = Requests.request(RequestContext.builder()
                .url(apiURL)
                .build());

        ErrorResult mayError = this.processErrorResponse(response, ResolveResult.Source.GITHUB);

        if (mayError != null)
            return mayError;

        if (tag != null)
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
                if (errorResult.getCause() == ErrorResult.ErrorCause.VERSION_MISMATCH)
                    isFound = true;
                else if (errorResult.getCause() == ErrorResult.ErrorCause.ASSET_NOT_FOUND)
                    isNoAssets = true;
                continue;
            }

            results.add(result);
        }

        if (results.isEmpty())
            if (isFound)
                return new ErrorResult(
                        this,
                        ErrorResult.ErrorCause.VERSION_MISMATCH,
                        ResolveResult.Source.GITHUB
                );
            else if (isNoAssets)
                return new ErrorResult(
                        this,
                        ErrorResult.ErrorCause.ASSET_NOT_FOUND,
                        ResolveResult.Source.GITHUB
                );


        return new MultiResult(this, results.toArray(new ResolveResult[0]));
    }

    private ResolveResult buildResultSingle(String owner, String repositoryName, JsonObject object, @Nullable String queryVersion)
    {
        List<GitHubSuccessResult> results = new ArrayList<>();

        String releaseName = object.get("name").getAsString();
        String body = object.get("body").getAsString();
        String version = object.get("tag_name").getAsString();
        long releaseId = object.get("id").getAsLong();

        if (queryVersion != null && !queryVersion.equalsIgnoreCase(version) &&
                !("v" + queryVersion).equalsIgnoreCase(queryVersion) && !queryVersion.equalsIgnoreCase("v" + queryVersion))
            return new ErrorResult(this, ErrorResult.ErrorCause.VERSION_MISMATCH, ResolveResult.Source.GITHUB);

        JsonArray assets = object.getAsJsonArray("assets");

        if (assets.size() == 0)
            return new ErrorResult(this, ErrorResult.ErrorCause.ASSET_NOT_FOUND, ResolveResult.Source.GITHUB);

        for (JsonElement asset : assets)
        {
            if (!asset.isJsonObject())
                continue;

            JsonObject assetObject = asset.getAsJsonObject();

            String downloadURL = assetObject.get("browser_download_url").getAsString();
            String fileName = assetObject.get("name").getAsString();
            long size = assetObject.get("size").getAsLong();

            GitHubSuccessResult result = new GitHubSuccessResult(this, downloadURL, fileName, version, repositoryName, owner, size, releaseName, body, releaseId);

            if (assets.size() == 1)
                return result;
            else
                results.add(result);
        }

        return new MultiResult(this, results.toArray(new GitHubSuccessResult[0]));
    }

    @Override
    public String[] getHosts()
    {
        return new String[] { "github.com", "www.github.com" };
    }
}
