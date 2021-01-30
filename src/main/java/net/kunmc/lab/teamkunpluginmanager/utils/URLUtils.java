package net.kunmc.lab.teamkunpluginmanager.utils;


import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() != 200)
                return "[]";

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
            connection.connect();
            if (connection.getResponseCode() != 200)
                return "";

            try (DataInputStream dis = new DataInputStream(connection.getInputStream());
                 DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("plugins/" + fileName))))
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
