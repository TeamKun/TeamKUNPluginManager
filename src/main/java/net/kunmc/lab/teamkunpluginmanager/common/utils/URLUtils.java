package net.kunmc.lab.teamkunpluginmanager.common.utils;

import net.kunmc.lab.teamkunpluginmanager.common.Variables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
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
            if (!Variables.OAuthToken.isEmpty() && url.getHost().equals("api.github.com"))
                connection.setRequestProperty("Authorization", "token " + Variables.OAuthToken);
            if (url.getHost().equals("file.io"))
                connection.setRequestProperty("Referer", "https://www.file.io/");
            connection.setRequestProperty("User-Agent", "Mozilla/8.10; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop");
            connection.connect();

            switch (connection.getResponseCode())
            {
                case 404:
                    return "{'message':'プラグインが見つかりませんでした。'}";
                case 403:
                    return "{'message':'プラグインを取得できませんでした。15分たって解決しない場合はトークンないしリポジトリを変更してください。'}";
                case 401:
                    return "{'message':'プラグインを取得できませんでした。トークンが間違っている可能性があります。'}";
            }


            try(InputStream stream = connection.getInputStream())
            {
                String response = IOUtils.toString(stream, StandardCharsets.UTF_8);
                if (connection.getResponseCode() == 200)
                    return response;
                System.out.println("Code: " + connection.getResponseCode());
                System.out.println(response);
                return "{'message':'不明なエラーが発生しました。コンソールに結果をダンプします。'}";
            }

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
        while (new File((ClassUtils.isExists("org.bukkit.ChatColor") ? "pluguns/": "") + fileName).exists())
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
                connection.setRequestProperty("Authorization", "token " + Variables.OAuthToken);
            connection.setRequestProperty("User-Agent", "Mozilla/8.10; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop");
            connection.connect();
            if (connection.getResponseCode() != 200)
                return new Pair<>(false, "");

            FileUtils.copyInputStreamToFile(connection.getInputStream(), new File((ClassUtils.isExists("org.bukkit.ChatColor") ? "pluguns/": "") + fileName));
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
                connection.setRequestProperty("Authorization", "token " + Variables.OAuthToken);
            connection.setRequestProperty("User-Agent", "Mozilla/8.10; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop");
            connection.connect();
            return connection.getResponseCode();

        }
        catch (Exception e)
        {
            return 500;
        }
    }
}
