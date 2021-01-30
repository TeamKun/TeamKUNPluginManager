package net.kunmc.lab.teamkunpluginmanager.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.util.Pair;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubURLBuilder
{
    private static final String GITHUB_REPO_RELEASES_URL = "https://api.github.com/repos/%s/releases";
    private static final String GITHUB_REPO_RELEASE_NAME_URL = GITHUB_REPO_RELEASES_URL + "/tags/%s";

    /**
     * GitHubのリリースへのAPIのURlをビルドします。
     * tagName をnullにできます。
     *
     * @param repository リポジトリ名
     * @param tagName タグ(リリース)バージョン
     *
     * @throws NullPointerException repositoryがnullだった場合
     *
     * @return URlのタイプ, ビルドしたURL
     */
    private static Pair<String, String> buildAPIUrl(String repository, @Nullable String tagName)
    {
        if (repository == null)
            return new Pair<>("ERROR", "");

        if (tagName == null)
            return new Pair<>("GITHUB_REPO_RELEASES_URL", String.format(GITHUB_REPO_RELEASES_URL, repository));
        else
            return new Pair<>("GITHUB_REPO_RELEASE_NAME_URL", String.format(GITHUB_REPO_RELEASE_NAME_URL, repository, tagName));
    }

    private static final Pattern GITHUB_REPO_PATTERN = Pattern.compile("^/(?<repo>[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}/[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,100})/?((releases/?(tag/?)?)(?<tagName>.*)?)?$");
    /**
     * GitHubのURLを適正化
     * https://github.com/examples/ExamplePlugin => https://github.com/examples/ExamplePlugin/releases/download/1.0/ExamplePlugin-1.0.jar
     * https://example.com/ => https://example.com/
     * @param urlName 適当なURL
     * @return GitHubのURl
     */
    public static String urlValidate(String urlName)
    {
        URL url;
        try
        {
            url = new URL(urlName);
        }
        catch (Exception e)
        {
            return urlName;
        }

        if (!url.getHost().equals("github.com") && !url.getHost().equals("www.github.com"))
            return urlName;

        Matcher matcher = GITHUB_REPO_PATTERN.matcher(url.getPath());

        String repoName = null;
        String tagName = null;

        while(matcher.find())
        {
            String repository = matcher.group("repo");
            String tag = matcher.group("tagName");
            if (!repository.equals(""))
                repoName = repository;
            if (tag != null && !tag.equals(""))
                tagName = tag;
        }

        Pair<String, String> urlPair = buildAPIUrl(repoName, tagName);

        switch (urlPair.getKey())
        {
            case "GITHUB_REPO_RELEASES_URL":
            {
                String json = URLUtils.getAsString(urlPair.getValue());
                String error = error(json);
                if (!error.equals(""))
                    return "ERROR " + error;
                JsonArray array = new Gson().fromJson(json, JsonArray.class);

                for(JsonElement elem: array)
                    for(JsonElement asset: ((JsonObject) elem).get("assets").getAsJsonArray())
                        return ((JsonObject) asset).get("browser_download_url").getAsString();
                return "ERROR " + json;
            }
            case "GITHUB_REPO_RELEASE_NAME_URL":
            {
                String json = URLUtils.getAsString(urlPair.getValue());
                String error = error(json);
                if (!error.equals(""))
                    return "ERROR " + error;
                JsonObject array = new Gson().fromJson(json, JsonObject.class);
                for(JsonElement asset: array.get("assets").getAsJsonArray())
                    return ((JsonObject) asset).get("browser_download_url").getAsString();
                return "ERROR Unknown";
            }
            case "ERROR":
            default:
                return "ERROR Unknown";
        }
    }

    private static String error(String json)
    {
        try
        {
            JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
            if (!jsonObject.has("message"))
                return "";
            return jsonObject.get("message").getAsString();
        }
        catch (Exception ignored)
        {
            return "";
        }
    }
}
