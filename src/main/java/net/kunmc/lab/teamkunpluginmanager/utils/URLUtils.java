package net.kunmc.lab.teamkunpluginmanager.utils;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;

public class URLUtils
{
    /**
     * URLにPOSTした結果を返す。
     *
     * @param urlString URL
     * @param data      データ
     * @param accept    受け入れるタイプ。 application/
     * @return レスポンスコード, 結果
     */
    public static Pair<Integer, String> postAsString(String urlString, String data, String accept, String content_type)
    {
        try
        {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            if (url.getHost().endsWith("github.com") || url.getHost().equals("raw.githubusercontent.com"))
                connection.setRequestProperty(
                        "Authorization",
                        "token " + TeamKunPluginManager.vault.getToken()
                );
            if (url.getHost().equals("file.io"))
                connection.setRequestProperty("Referer", "https://www.file.io/");
            connection.setRequestProperty("User-Agent", "Mozilla/8.10; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop");
            connection.setRequestProperty("Accept", accept);
            connection.setRequestProperty("Content-Type", content_type);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.connect();

            try (OutputStream os = connection.getOutputStream())
            {
                PrintStream prtstr = new PrintStream(os);
                prtstr.println(data);
                prtstr.close();
                int resp = connection.getResponseCode();
                if (resp < 200 || resp > 299)
                    return new Pair<>(resp, "");

                try (InputStream is = connection.getInputStream())
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
     *
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
            if ((url.getHost().endsWith("github.com") || url.getHost().equals("raw.githubusercontent.com")) &&
                    !TeamKunPluginManager.vault.getToken().isEmpty())
                connection.setRequestProperty(
                        "Authorization",
                        "token " + TeamKunPluginManager.vault.getToken()
                );
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
        while (new File("plugins/" + fileName).exists())
        {
            fileName = original + "." + ++tryna + ".jar";
            duplicateFile = true;
        }

        tryna = 0;

        final int redirectLimit = TeamKunPluginManager.config.getInt("redirectLimit", 15);

        try
        {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            boolean redir;
            do
            {
                try
                {
                    if (tryna++ > redirectLimit)
                        return new Pair<>(null, "ERROR リダイレクトリミットに到達しました。");

                    connection.setRequestMethod("GET");
                    connection.setInstanceFollowRedirects(false);
                    if (urlObj.getHost().endsWith("github.com") || urlObj.getHost().equals("raw.githubusercontent.com"))
                        connection.setRequestProperty(
                                "Authorization",
                                "token " + TeamKunPluginManager.vault.getToken()
                        );
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
                catch (UnknownHostException e)
                {
                    return new Pair<>(null, "ERROR '" + urlObj.getHost() + "' を解決できませんでした。");
                }
                catch (ClassCastException e)
                {
                    return new Pair<>(null, "ERROR プロトコルが壊れています。");
                }
                catch (Exception e)
                {
                    return new Pair<>(null, "ERROR エラー '" + e.getClass().getName() + "' が発生しました。");
                }
            }
            while (redir);

            File file = new File("plugins/" + fileName);

            if (!file.createNewFile())
                throw new NoSuchFileException("ファイルの作成に失敗しました。");

            try (InputStream is = connection.getInputStream();
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
            if (url.getHost().endsWith("github.com") || url.getHost().equals("raw.githubusercontent.com"))
                connection.setRequestProperty(
                        "Authorization",
                        "token " + TeamKunPluginManager.vault.getToken()
                );
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
