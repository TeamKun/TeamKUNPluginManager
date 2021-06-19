package net.kunmc.lab.teamkunpluginmanager.utils;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashLib
{
    public static String genSha1(File file, int length)
    {
        String digest;
        if ((digest = genSha1(file)).length() <= length)
            return digest;
        return digest.substring(0, length);
    }

    public static String genSha1(File file)
    {
        if (!file.exists())
            return "";


        try(DigestInputStream di =
                    new DigestInputStream(
                            new BufferedInputStream(new FileInputStream(file)),
                            MessageDigest.getInstance("SHA-1")))
        {
            int count = 0;
            while(di.read() != -1 && count++ < Integer.MAX_VALUE);

            char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
            byte[] bytes = di.getMessageDigest().digest();
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = HEX_ARRAY[v >>> 4];
                hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
            }

            return new String(hexChars);
        }
        catch (IOException | NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return "";
        }
    }
}
