package net.kunmc.lab.teamkunpluginmanager;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TokenVault
{

    private String token;

    public TokenVault()
    {
        token = "";

        if (!new File(new File("").getAbsolutePath(), "kpm.vault").exists())
            return;
        try
        {
            token = FileUtils.readFileToString(new File(new File("").getAbsolutePath(), "kpm.vault"), StandardCharsets.UTF_8);
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
            FileUtils.writeStringToFile(new File(new File("").getAbsolutePath(), "kpm.vault"), token, Charset.defaultCharset());
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
