package net.kunmc.lab.teamkunpluginmanager.resolver.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.URLResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult;
import net.kunmc.lab.teamkunpluginmanager.utils.Pair;
import net.kunmc.lab.teamkunpluginmanager.utils.URLUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubResolver implements URLResolver
{
    private static final String GITHUB_REPO_RELEASES_URL = "https://api.github.com/repos/%s/releases";
    private static final String GITHUB_REPO_RELEASE_NAME_URL = GITHUB_REPO_RELEASES_URL + "/tags/%s";

    private static final Pattern GITHUB_REPO_PATTERN = Pattern.compile("^/(?<repository>[a-zA-Z\\d]" +
            "(?<owner>[a-zA-Z\\d]|-(?=[a-zA-Z\\d])){0,38}/[a-zA-Z\\d](?:[a-zA-Z\\d]|-(?=[a-zA-Z\\d])){0,100})" +
            "(?:/(?:tags|releases(?:/(?:tag/(?<tag>[^/]+)/?$|download/(?<downloadTag>[^/]+)/" +
            "(?<fileName>[^/]+)))?))?/?$");

    @Override
    public ResolveResult resolve(String query)
    {
        URL url;
        try
        {

            url = new URL(query);
        }
        catch (MalformedURLException e)
        {
            return new ErrorResult(ErrorResult.ErrorCause.INVALID_QUERY, ResolveResult.Source.GITHUB);
        }

        Matcher matcher = GITHUB_REPO_PATTERN.matcher(url.getPath());

        String repository = null;
        String owner = null;
        String tag = null;

        while (matcher.find())
        {
            String repositoryGroup = matcher.group("repository");
            String downloadTagGroup = matcher.group("downloadTag");
            String tagGroup = matcher.group("tag");
            String fileNameGroup = matcher.group("fileName");
            String ownerGroup = matcher.group("owner");

            if (fileNameGroup != null && !fileNameGroup.isEmpty()) // URLが自己解決。
                return new SuccessResult(url.getPath(), ResolveResult.Source.GITHUB);

            if (!repositoryGroup.isEmpty())
                repository = repositoryGroup;

            if (downloadTagGroup != null && !downloadTagGroup.isEmpty())
                tag = downloadTagGroup;
            else if (tagGroup != null && !tagGroup.isEmpty())
                tag = tagGroup;

            if (ownerGroup != null && !ownerGroup.isEmpty())
                owner = ownerGroup;
        }

        if (repository == null)
            return new ErrorResult(ErrorResult.ErrorCause.INVALID_QUERY, ResolveResult.Source.GITHUB);

        return processGitHubAPI(owner, repository, tag);
    }

    @Override
    public ResolveResult autoPickOnePlugin(MultiResult multiResult)
    {
        return autoPickFirst(multiResult, ResolveResult.Source.GITHUB);
    }

    private ResolveResult processGitHubAPI(String owner, String repository, String tag)
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

            return buildResultSingle(owner, jsonObject);
        }

        JsonArray jsonArray = gson.fromJson(json, JsonArray.class);
        List<ResolveResult> results = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray)
        {
            ResolveResult result = buildResultSingle(owner, jsonElement.getAsJsonObject());

            if (result instanceof ErrorResult)
                continue;

            results.add(result);
        }

        return new MultiResult(results.toArray(new ResolveResult[0]));
    }

    private ResolveResult buildResultSingle(String owner, JsonObject object)
    {
        List<GitHubSuccessResult> results = new ArrayList<>();

        String releaseName = object.get("name").getAsString();
        String body = object.get("body").getAsString();
        String version = object.get("tag_name").getAsString();
        long releaseId = object.get("id").getAsLong();

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

            GitHubSuccessResult result = new GitHubSuccessResult(downloadURL, fileName, version, owner, size, releaseName, body, releaseId);

            if (assets.size() == 1)
                return result;
            else
                results.add(result);
        }

        return new MultiResult(results.toArray(new GitHubSuccessResult[0]));
    }

    @Override
    public Pattern[] getURLPatterns()
    {
        return new Pattern[] {
                Pattern.compile("^https?://(www\\.)?github.com/")
        };
    }

}
