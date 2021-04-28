package net.kunmc.lab.teamkunpluginmanager.utils.rdmarker;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kunmc.lab.teamkunpluginmanager.utils.URLUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DevBukkit
{

    private static final String basePatterns = "(?<slug>\\w+)(/(files|/screenshots|relations/(dependencies|dependents)|)/?$)?(/?$|/files(/(?<id>\\d+)((/files/(?<version>\\d+))?/download)?)?)/?$";
    private static final Pattern BUKKIT_PATTERN = Pattern.compile("^/projects/" + basePatterns);
    private static final Pattern CURSE_PATTERN = Pattern.compile("^/minecraft/bukkit-plugins/" + basePatterns);

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

        boolean isBukkit;

        if (!(isBukkit = url.getHost().equals("dev.bukkit.org")) && !url.getHost().equals("curseforge.com"))
            return false;

        Matcher matcher = (isBukkit ? BUKKIT_PATTERN: CURSE_PATTERN).matcher(url.getPath());

        while(matcher.find())
            if (matcher.group("slug") != null && !matcher.group("slug").equals(""))
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

        if (!url.getHost().equals("dev.bukkit.org"))
            return urlName;

        Matcher matcher = BUKKIT_PATTERN.matcher(url.getPath());

        String slug = null;
        String version = null;

        while(matcher.find())
        {
            if (matcher.group("slug") != null && !matcher.group("slug").equals(""))
                slug = matcher.group("slug");
            if (matcher.group("version") != null && !matcher.group("version").equals(""))
                version = matcher.group("version");
        }

        if (slug == null)
            return urlName;
        JsonArray projectSearchResult = new Gson().fromJson(
                URLUtils.getAsString("https://servermods.forgesvc.net/servermods/projects?search=" + slug),
                JsonArray.class);

        String id = null;

        for(JsonElement elm: projectSearchResult)
        {
            JsonObject obj = (JsonObject) elm;

            if (obj.get("slug").getAsString().equalsIgnoreCase(slug))
            {
                id = obj.get("id").getAsString();
                break;
            }
        }


        if (id == null)
            return urlName;

        JsonArray files = new Gson().fromJson(
                URLUtils.getAsString("https://servermods.forgesvc.net/servermods/files?projectIds=" + id),
                JsonArray.class);

        String apiVersion = StringUtils.split(Bukkit.getVersion(), "-")[0];

        String downloadUrl = null;

        for(JsonElement elm: files)
        {
            JsonObject obj = (JsonObject) elm;

            if (version != null)
            {
                if (obj.get("fileUrl").getAsString().endsWith(version))
                {
                    downloadUrl = obj.get("downloadUrl").getAsString();
                    break;
                }
            }
            else
            {
                if (apiVersion.contains(obj.get("gameVersion").getAsString()))
                {
                    downloadUrl = obj.get("downloadUrl").getAsString();
                    break;
                }
            }
        }

        if (downloadUrl == null && files.size() > 0)
            downloadUrl = ((JsonObject) files.get(files.size() - 1)).get("downloadUrl").getAsString();

        if (downloadUrl == null)
            return urlName;

        return downloadUrl;
    }
}
