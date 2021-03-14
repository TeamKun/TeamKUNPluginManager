package net.kunmc.lab.teamkunpluginmanager.common;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TokenVault
{

    private String token;
    private File file;

    public TokenVault(File file)
    {
        token = "";

        this.file = file;
        if (!file.exists())
            return;
        try
        {
            token = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            System.out.println("TOKENの読み込みに失敗しました。");
            e.printStackTrace();
        }
    }

    public synchronized void vault(String token)
    {
        this.token = token;
        try
        {
            FileUtils.writeStringToFile(file, token, Charset.defaultCharset());
        }
        catch (IOException e)
        {
            System.out.println("TOKENの保管に失敗しました。");
            e.printStackTrace();
        }
    }

    public String getToken()
    {
        return token;
    }
}
