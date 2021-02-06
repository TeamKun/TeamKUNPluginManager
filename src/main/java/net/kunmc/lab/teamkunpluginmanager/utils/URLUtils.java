package net.kunmc.lab.teamkunpluginmanager.utils;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class URLUtils
{
    public static String getAsString(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (url.getHost().equals("api.github.com"))
                connection.setRequestProperty("Authorization", "token " + TeamKunPluginManager.config.getString("oauth"));
            if (url.getHost().equals("file.io"))
                connection.setRequestProperty("Referer", "https://www.file.io/");
            connection.setRequestProperty("User-Agent", "Mozilla/1.14.5.14; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop");
            connection.connect();

            if (connection.getResponseCode() == 404)
                return "{'message': 'プラグインが見つかりませんでした。'}";
            if (connection.getResponseCode() == 403)
                return "{'message': 'プラグインを取得できません。しばらくしてからもう一度インストールしてください。'}";

            if (connection.getResponseCode() != 200)
                return "{'message': '不明なエラーが発生しました。'}";

            return IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "[]";
        }
    }

    /**
     * ファイルをだうんろーど！
     *
     * @param url URL
     * @return ローカルのパス
     */
    public static Pair<Boolean, String> downloadFile(String url)
    {
        String fileName = url.substring(url.lastIndexOf("/") + 1);

        boolean duplicateFile = false;

        if (fileName.equals(""))
            fileName = "tmp1.jar";

        int tryna = 0;
        String original = fileName;
        while (new File("plugins/" + fileName).exists())
        {
            fileName = original + "." + ++tryna + ".jar";
            duplicateFile = true;
        }

        try
        {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            if (urlObj.getHost().equals("api.github.com"))
                connection.setRequestProperty("Authorization", "token " + TeamKunPluginManager.config.getString("oauth"));
            connection.setRequestProperty("User-Agent", "Mozilla/1.14.5.14; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop");
            connection.connect();
            if (connection.getResponseCode() != 200)
                return new Pair<>(false, "");

            FileUtils.copyURLToFile(urlObj, new File("plugins/" + fileName));
            return new Pair<>(duplicateFile, fileName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Pair<>(false, "");

        }
    }

    public static int fetch(String urlString, String method)
    {
        try
        {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            if (url.getHost().equals("api.github.com"))
                connection.setRequestProperty("Authorization", "token " + TeamKunPluginManager.config.getString("oauth"));
            connection.setRequestProperty("User-Agent", "Mozilla/1.14.5.14; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop");
            connection.connect();
            return connection.getResponseCode();

        }
        catch (Exception e)
        {
            return 500;
        }
    }
}
