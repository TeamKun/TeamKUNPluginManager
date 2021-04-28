package net.kunmc.lab.teamkunpluginmanager.common.utils;

import net.kunmc.lab.teamkunpluginmanager.common.Variables;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;

public class URLUtils
{
    /**
     * リダイレクトのリミット
     */
    public static int redirectLimit = 15;

    public static String getAsString(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (!Variables.vault.getToken().isEmpty() && url.getHost().equals("api.github.com"))
                connection.setRequestProperty("Authorization", "token " + Variables.vault.getToken());
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


            try (InputStream stream = connection.getInputStream())
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
        return downloadFile(url, url.substring(url.lastIndexOf("/") + 1));
    }

    /**
     * ファイルをだうんろーど！
     *
     * @param url      URL
     * @param fileName ファイル名
     * @return ローカルのパス
     */
    public static Pair<Boolean, String> downloadFile(String url, String fileName)
    {
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

        tryna = 0;

        try
        {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            boolean redir;
            do
            {
                if (tryna++ > redirectLimit)
                    throw new IOException("Too many redirects.");

                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(false);
                if (urlObj.getHost().equals("api.github.com"))
                    connection.setRequestProperty("Authorization", "token " + Variables.vault.getToken());
                connection.setRequestProperty("User-Agent", "Mozilla/8.10; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop");
                connection.connect();

                redir = false;
                if (String.valueOf(connection.getResponseCode()).startsWith("3"))
                {
                    URL base = connection.getURL();
                    String locationStr = connection.getHeaderField("Location");
                    if (locationStr != null)
                        base = new URL(base, locationStr);

                    //connection.disconnect();
                    if (base != null)
                    {
                        redir = true;
                        connection = (HttpURLConnection) base.openConnection();
                    }
                }

            }
            while (redir);

            File file = new File("plugins/" + fileName);

            if (!file.createNewFile())
                throw new NoSuchFileException("ファイルの作成に失敗しました。");

            try(InputStream is = connection.getInputStream();
                OutputStream os = new FileOutputStream(file))
            {
                IOUtils.copy(is, os);
            }


            //FileUtils.copyInputStreamToFile(connection.getInputStream(), file);
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
                connection.setRequestProperty("Authorization", "token " + Variables.vault.getToken());
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
