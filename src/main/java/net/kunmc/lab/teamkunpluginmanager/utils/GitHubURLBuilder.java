package net.kunmc.lab.teamkunpluginmanager.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GitHubURLBuilder
{
    /**
     * APIのURL。APIが変更されたら変更。
     */
    private static final String GITHUB_REPO_RELEASES_URL = "https://api.github.com/repos/%s/releases";

    /**
     * APIのURL。APIが変更されたら変更。
     */
    private static final String GITHUB_REPO_RELEASE_NAME_URL = GITHUB_REPO_RELEASES_URL + "/tags/%s";

    /**
     * GitHubのURLかどうかを判定/パースをする。
     */
    private static final Pattern GITHUB_REPO_PATTERN = Pattern.compile("^/(?<repo>[a-zA-Z\\d](?:[a-zA-Z\\d]|-(?=[a-zA-Z\\d])){0,38}/[a-zA-Z\\d](?:[a-zA-Z\\d]|-(?=[a-zA-Z\\d])){0,100})(?:/?$|/(?:tag|release)s(?:/?$|/(?:tag/(?<tag>[^/]+)/?$|download/(?<dlTag>[^/]+)/(?<fileName>[^/]+))))/?$");

    /**
     * GitHubのリリースへのAPIのURlをビルドします。
     * tagName をnullにできます。
     *
     * @param repository リポジトリ名
     * @param tagName    タグ(リリース)バージョン
     * @return URlのタイプ, ビルドしたURL
     * @throws NullPointerException repositoryがnullだった場合
     */
    private static Pair<String, String> buildAPIUrl(String repository, @Nullable String tagName)
    {
        //リポジトリがNullだった場合ERRORのみを返す。
        if (repository == null)
            return new Pair<>("ERROR", "");

        //タグがなかった場合はリリースすべてを返す。
        if (tagName == null)
            return new Pair<>("GITHUB_REPO_RELEASES_URL", String.format(GITHUB_REPO_RELEASES_URL, repository));
        else //タグがあったら/tags/までのリソースを返す。
            return new Pair<>("GITHUB_REPO_RELEASE_NAME_URL", String.format(GITHUB_REPO_RELEASE_NAME_URL, repository, tagName));
    }

    /**
     * GitHubのURLを適正化
     * https://github.com/examples/ExamplePlugin => https://github.com/examples/ExamplePlugin/releases/download/1.0/ExamplePlugin-1.0.jar
     * https://example.com/ => https://example.com/
     *
     * @param urlName GitHubの様々なURL
     * @return GitHubのURl
     */
    public static String urlValidate(String urlName, String version)
    {
        //URLかどうか検証。
        URL url;
        try
        {
            url = new URL(urlName);
        }
        catch (Exception e) //URLではない場合はオウム返し。
        {
            return urlName;
        }

        //URLのホストが github.com もしくは www.github.com かどうか。
        if (!url.getHost().equals("github.com") && !url.getHost().equals("www.github.com"))
            return urlName;

        //パターンとマッチするか。
        Matcher matcher = GITHUB_REPO_PATTERN.matcher(url.getPath());

        String repoName = null;
        String tagName = null;

        //グループごと。
        while (matcher.find())
        {
            String repository = matcher.group("repo");
            String tag = matcher.group("dlTag");
            String tagNF = matcher.group("tag");
            String file = matcher.group("fileName");

            //ファイルがない、もしくは""だった場合はオウム返し。
            if (file != null && !file.equals(""))
                return urlName;

            //この辺は代入なので無視。
            if (!repository.equals(""))
                repoName = repository;
            if (tag != null && !tag.equals(""))
                tagName = tag;
            else if (tagNF != null && !tagNF.equals(""))
                tagName = tagNF;
        }

        //tagがなく、version(@で区切られるやつ)がある場合はこちらを優先。
        if (tagName == null && version != null)
            tagName = version;

        //URLをペアから作る。
        Pair<String, String> urlPair = buildAPIUrl(repoName, tagName);

        switch (urlPair.getKey())
        {
            case "GITHUB_REPO_RELEASES_URL":
            {
                //APIを叩く。
                Pair<Integer, String> json = URLUtils.getAsString(urlPair.getValue());
                switch (json.getKey())
                {
                    case 404:
                        return "ERROR ファイルが見つかりませんでした。";
                    case 403:
                        return "ERROR ファイルを取得できません。しばらくしてからもう一度実行してください。";
                }

                //200ではない場合はエラー。
                if (json.getKey() != 200)
                    return "ERROR 不明なエラーが発生しました。";

                //APIレスポンスがエラーかどうか。
                String error = error(json.getValue());
                if (!error.equals(""))
                    return "ERROR " + error;

                JsonArray array = new Gson().fromJson(json.getValue(), JsonArray.class);

                if (array.size() == 0)
                    return "ERROR リリースが見つかりませんでした。";

                //最新(i:0)をとり、assetsまでもってくる。
                JsonArray asset = ((JsonObject) array.get(0)).get("assets").getAsJsonArray();

               if (array.size() == 0)
                   return "ERROR アーティファクトが見つかりませんでした。";

               //assetが一つしかなかったら返す。
               if (asset.size() == 1)
                   return ((JsonObject) asset.get(0)).get("browser_download_url").getAsString();

               //assetが２つ以上あるのでMULTIフラグを立てて返す。
                return "MULTI " + StreamSupport.stream(asset.spliterator(), true)
                        .map(element -> {
                            JsonObject obj = (JsonObject) element;
                            String assetName = obj.get("name").getAsString();
                            String assetDownloadUrl = obj.get("browser_download_url").getAsString();
                            return assetName.replace("|", "\\|") + "|" + assetDownloadUrl.replace("|", "\\|");
                        })
                        .collect(Collectors.joining("|")) + "|";


            }
            case "GITHUB_REPO_RELEASE_NAME_URL":
            {
                //APIを叩く。
                Pair<Integer, String> json = URLUtils.getAsString(urlPair.getValue());
                switch (json.getKey())
                {
                    case 404:
                        return "ERROR ファイルが見つかりませんでした。";
                    case 403:
                        return "ERROR ファイルを取得できません。しばらくしてからもう一度実行してください。";
                }

                //200ではない場合はエラー。
                if (json.getKey() != 200)
                    return "ERROR 不明なエラーが発生しました。";

                //APIレスポンスがエラーかどうか。
                String error = error(json.getValue());
                if (!error.equals(""))
                    return "ERROR " + error;

                //アセットを上からなめる
                JsonObject array = new Gson().fromJson(json.getValue(), JsonObject.class);
                JsonArray asset = array.get("assets").getAsJsonArray();

                //assetが一つしかなかったら返す。
                if (asset.size() == 1)
                    return ((JsonObject) asset.get(0)).get("browser_download_url").getAsString();

                //assetが２つ以上あるのでMULTIフラグを立てて返す。
                return "MULTI " + StreamSupport.stream(asset.spliterator(), true)
                        .map(element -> {
                            JsonObject obj = (JsonObject) element;
                            String assetName = obj.get("name").getAsString();
                            String assetDownloadUrl = obj.get("browser_download_url").getAsString();
                            return assetName.replace("|", "\\|") + "|" + assetDownloadUrl.replace("|", "\\|");
                        })
                        .collect(Collectors.joining("|")) + "|";
            }
            case "ERROR": //ERRORだった場合はオウム返し。
            default:
                return url.toString();
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

    public static boolean isRepoExists(String name)
    {
        return String.valueOf(URLUtils.fetch("https://api.github.com/repos/" + name + "", "HEAD")).startsWith("2");
    }

    public static boolean isPermissionGranted(String path)
    {
        return URLUtils.fetch("https://api.github.com" + path, "HEAD") != 401;
    }

    public static class BuildResult
    {
        public String name;
        public String url;
    }
}
