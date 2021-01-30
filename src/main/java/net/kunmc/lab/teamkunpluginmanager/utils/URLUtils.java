package net.kunmc.lab.teamkunpluginmanager.utils;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
            if (url.getHost().equals("api.github.com"))
                connection.setRequestProperty("Authorization", "token " + TeamKunPluginManager.config.getString("oauth"));
            connection.setRequestProperty("User-Agent", "TeamKUN Client");
            connection.connect();
            if (connection.getResponseCode() != 200 && connection.getResponseCode() != 404 && connection.getResponseCode() != 403)
                return "[]";

            String a = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
            return a;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "[]";
        }
    }

    /**
     * ファイルをだうんろーど！
     * @param url URL
     * @return ローカルのパス
     */
    public static String downloadFile(String url)
    {
        String fileName = url.substring(url.lastIndexOf("/") + 1);

        if (fileName.equals(""))
            fileName = "tmp1.jar";

        try
        {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            if (urlObj.getHost().equals("api.github.com"))
                connection.setRequestProperty("Authorization", "token " + TeamKunPluginManager.config.getString("oauth"));
            connection.setRequestProperty("User-Agent", "TeamKUN Client");
            connection.connect();
            if (connection.getResponseCode() != 200)
                return "";

            try (InputStream stream = connection.getInputStream();
                 DataInputStream dis = new DataInputStream(stream);
                 FileOutputStream fos = new FileOutputStream("plugins/" + fileName);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fos);
                 DataOutputStream dos = new DataOutputStream(bufferedOutputStream))
            {
                byte[] b = new byte[1919];
                int read;
                while ((read = dis.read(b)) != -1)
                    dos.write(b, 0, read);
            }
            return fileName;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }
}
