package net.kunmc.lab.teamkunpluginmanager.common.utils;

import net.kunmc.lab.teamkunpluginmanager.common.Variables;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class URLUtils
{
    /**
     * リダイレクトのリミット
     */
    public static int redirectLimit = 15;

    /**
     * URLにPOSTした結果を返す。
     * @param urlString URL
     * @param data データ
     * @param accept 受け入れるタイプ。 application/
     * @return レスポンスコード, 結果
     */
    public static Pair<Integer, String> postAsString(String urlString, String data, String accept, String content_type)
    {
        try
        {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            if (url.getHost().equals("api.github.com"))
                connection.setRequestProperty("Authorization", "token " + Variables.vault.getToken());
            if (url.getHost().equals("file.io"))
                connection.setRequestProperty("Referer", "https://www.file.io/");
            connection.setRequestProperty("User-Agent", "Mozilla/8.10; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop");
            connection.setRequestProperty("Accept", accept);
            connection.setRequestProperty("Content-Type", content_type);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.connect();

            try(OutputStream os = connection.getOutputStream();)
            {
                PrintStream prtstr = new PrintStream(os);
                prtstr.println(data);
                prtstr.close();
                int resp = connection.getResponseCode();
                if (resp < 200 || resp > 299)
                    return new Pair<>(resp, "");

                try(InputStream is = connection.getInputStream();)
                {
                    return new Pair<>(resp, IOUtils.toString(is, StandardCharsets.UTF_8));
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Pair<>(-1, "");
        }
    }

    /**
     * URLから取得した結果を返す。
     * @param urlString URL
     * @return レスポンスコード, 結果
     */
    public static Pair<Integer, String> getAsString(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (!Variables.vault.getToken().isEmpty() && url.getHost().equals("api.github.com"))
                connection.setRequestProperty("Authorization", "token " + Variables.vault.getToken().replace("\r", "").replace("\n", ""));
            if (url.getHost().equals("file.io"))
                connection.setRequestProperty("Referer", "https://www.file.io/");
            connection.setRequestProperty("User-Agent", "Mozilla/8.10; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop");
            connection.connect();

            return new Pair<>(connection.getResponseCode(), IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8));

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Pair<>(-1, "");
        }
    }

    /**
     * ファイルをだうんろーど！
     *
     * @param url URL
     * @return ローカルのパス
     * @param absolutePath 置き場所(絶対パス)
     */
    public static Pair<Boolean, String> downloadFile(String url, Path absolutePath)
    {
        return downloadFile(url, url.substring(url.lastIndexOf("/") + 1), absolutePath);
    }

    /**
     * ファイルをだうんろーど！
     *
     * @param url      URL
     * @param fileName ファイル名
     * @param absolutePath 置き場所(絶対パス)
     * @return ローカルのパス
     */
    public static Pair<Boolean, String> downloadFile(String url, String fileName, Path absolutePath)
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

            File file = new File(absolutePath.toFile(), fileName);


            if (file.exists()) //重複
                return new Pair<>(duplicateFile, fileName);

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
