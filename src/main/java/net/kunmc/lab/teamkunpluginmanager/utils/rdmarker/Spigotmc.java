package net.kunmc.lab.teamkunpluginmanager.utils.rdmarker;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Spigotmc
{

    private static final Pattern PATTERN = Pattern.compile("^/resources/(?<name>.+?\\.)?(?<id>\\d+)(/|/download(/|\\?/version=(?<version>\\d+))?)?$");

    public static boolean isMatch(String urlName)
    {
        URL url;
        try
        {
            url = new URL(urlName);
        }
        catch (Exception e)
        {
            return false;
        }

        if (!url.getHost().equals("spigotmc.org") && !url.getHost().equals("www.spigotmc.org"))
            return false;

        Matcher matcher = PATTERN.matcher(url.getPath());

        while(matcher.find())
            if (matcher.group("id") != null && !matcher.group("id").equals(""))
                return true;
        return false;
    }

    public static String toDownloadUrl(String urlName)
    {
        URL url;
        try
        {
            url = new URL(urlName);
        }
        catch (Exception e)
        {
            return urlName;
        }

        if (!url.getHost().equals("spigotmc.org") && !url.getHost().equals("www.spigotmc.org"))
            return urlName;

        Matcher matcher = PATTERN.matcher(url.getPath());

        String id = null;
        String name = null;
        String version = null;

        while(matcher.find())
        {
            if (matcher.group("id") != null && !matcher.group("id").equals(""))
                id = matcher.group("id");
            if (matcher.group("name") != null && !matcher.group("name").equals(""))
                name = matcher.group("name");
            if (matcher.group("version") != null && !matcher.group("version").equals(""))
                version = matcher.group("version");
        }

        if (id == null)
            return urlName;

        String baseUrl = "https://apple.api.spiget.org/v2/resources/" + id + "/";

        if (version == null)
            return baseUrl + "download";
        else
            return baseUrl + "versions/" + version + "/download";
    }
}
