package net.kunmc.lab.teamkunpluginmanager.resolver.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kunmc.lab.teamkunpluginmanager.resolver.QueryContext;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.URLResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult;
import net.kunmc.lab.teamkunpluginmanager.utils.Pair;
import net.kunmc.lab.teamkunpluginmanager.utils.URLUtils;
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
            "(?:[a-zA-Z\\d]|-(?=[a-zA-Z\\d])){0,38})/(?<repoName>[a-zA-Z\\d](?:[a-zA-Z\\d]|-(?=[a-zA-Z\\d])){0,100}))" +
            "(?:/(?:tags|releases(?:/(?:tag/(?<tag>[^/]+)/?$|download/(?<downloadTag>[^/]+)/" +
            "(?<fileName>[^/]+)))?))?/?$");

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        Matcher matcher = urlMatcher(GITHUB_REPO_PATTERN, query.getQuery());

        if (matcher == null)
            return new ErrorResult(ErrorResult.ErrorCause.INVALID_QUERY, ResolveResult.Source.GITHUB);

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
                return new SuccessResult(query.getQuery(), ResolveResult.Source.GITHUB);

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
            return new ErrorResult(ErrorResult.ErrorCause.INVALID_QUERY, ResolveResult.Source.GITHUB);

        return processGitHubAPI(owner, repositoryName, repository, tag, query.getVersion());
    }

    @Override
    public ResolveResult autoPickOnePlugin(MultiResult multiResult)
    {
        return autoPickFirst(multiResult, ResolveResult.Source.GITHUB);
    }

    private ResolveResult processGitHubAPI(String owner, String repositoryName, String repository, String tag, @Nullable String version)
    {
        String apiURL;
        if (tag != null)
            apiURL = String.format(GITHUB_REPO_RELEASE_NAME_URL, repository, tag);
        else
            apiURL = String.format(GITHUB_REPO_RELEASES_URL, repository);

        Pair<Integer, String> response = URLUtils.getAsString(apiURL);

        ErrorResult mayError = processErrorResponse(response.getLeft(), ResolveResult.Source.GITHUB);

        if (mayError != null)
            return mayError;

        String json = response.getRight();

        Gson gson = new Gson();

        if (tag != null)
        {
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

            return buildResultSingle(owner, repositoryName, jsonObject, version);
        }

        JsonArray jsonArray = gson.fromJson(json, JsonArray.class);
        List<ResolveResult> results = new ArrayList<>();

        boolean isFound = false;
        for (JsonElement jsonElement : jsonArray)
        {
            ResolveResult result = buildResultSingle(owner, repositoryName, jsonElement.getAsJsonObject(), version);

            if (result instanceof ErrorResult)
            {
                if (((ErrorResult) result).getCause() == ErrorResult.ErrorCause.MATCH_PLUGIN_NOT_FOUND)
                    isFound = true;
                continue;
            }

            results.add(result);
        }

        if (results.isEmpty() && isFound)
            return new ErrorResult(
                    ErrorResult.ErrorCause.MATCH_PLUGIN_NOT_FOUND.value("指定されたバージョンが見つかりませんでした。"),
                    ResolveResult.Source.GITHUB
            );

        return new MultiResult(results.toArray(new ResolveResult[0]));
    }

    private ResolveResult buildResultSingle(String owner, String repositoryName, JsonObject object, @Nullable String queryVersion)
    {
        List<GitHubSuccessResult> results = new ArrayList<>();

        String releaseName = object.get("name").getAsString();
        String body = object.get("body").getAsString();
        String version = object.get("tag_name").getAsString();
        long releaseId = object.get("id").getAsLong();
        String htmlUrl = object.get("html_url").getAsString();

        if (queryVersion != null && !queryVersion.equalsIgnoreCase(version) &&
                ("v" + queryVersion).equalsIgnoreCase(queryVersion) && !queryVersion.equalsIgnoreCase("v" + queryVersion))
            return new ErrorResult(ErrorResult.ErrorCause.MATCH_PLUGIN_NOT_FOUND, ResolveResult.Source.GITHUB);

        JsonArray assets = object.getAsJsonArray("assets");

        if (assets.size() == 0)
            return new ErrorResult(ErrorResult.ErrorCause.ASSET_NOT_FOUND, ResolveResult.Source.GITHUB);

        for (JsonElement asset : assets)
        {
            if (!asset.isJsonObject())
                continue;

            JsonObject assetObject = asset.getAsJsonObject();

            String downloadURL = assetObject.get("browser_download_url").getAsString();
            String fileName = assetObject.get("name").getAsString();
            long size = assetObject.get("size").getAsLong();

            GitHubSuccessResult result = new GitHubSuccessResult(downloadURL, fileName, version, repositoryName, htmlUrl, owner, size, releaseName, body, releaseId);

            if (assets.size() == 1)
                return result;
            else
                results.add(result);
        }

        return new MultiResult(results.toArray(new GitHubSuccessResult[0]));
    }

    @Override
    public String[] getHosts()
    {
        return new String[] { "github.com", "www.github.com" };
    }
}
