package net.kunmc.lab.teamkunpluginmanager.utils;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.NoSuchFileException;

public class URLUtils
{
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

        if (fileName.isEmpty())
            fileName = "tmp1.jar";

        int tryna = 0;
        String original = fileName;
        while (new File("plugins/" + fileName).exists())
        {
            fileName = original + "." + ++tryna + ".jar";
            duplicateFile = true;
        }

        tryna = 0;

        final int redirectLimit = TeamKunPluginManager.getPlugin().getPluginConfig().getInt("redirectLimit", 15);

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
                                "token " + TeamKunPluginManager.getPlugin().getVault().getToken()
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
            catch (Exception e)
            {
                e.printStackTrace();
                file.delete();
                return new Pair<>(null, "ERROR エラー '" + e.getClass().getName() + "' が発生しました。");
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
}
