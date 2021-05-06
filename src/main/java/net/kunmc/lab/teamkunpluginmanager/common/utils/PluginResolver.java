package net.kunmc.lab.teamkunpluginmanager.common.utils;

import net.kunmc.lab.teamkunpluginmanager.common.known.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.common.utils.rdMarker.DevBukkit;
import net.kunmc.lab.teamkunpluginmanager.common.utils.rdMarker.Spigotmc;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.List;

public class PluginResolver
{
    /**
     * プラグインのURLを名前解決します。
     *
     * @param query 指定
     * @return URLまたはError
     */
    public static String asUrl(Object ghName, String query)
    {
        String[] q = StringUtils.split(query, "@");

        String s = q[0];
        String ver = q.length > 1 ? q[1]: null;


        if (UrlValidator.getInstance().isValid(query))
        {
            if (DevBukkit.isMatch(query))
                return DevBukkit.toDownloadUrl(query);
            else if (Spigotmc.isMatch(query))
                return Spigotmc.toDownloadUrl(query);
            return GitHubURLBuilder.urlValidate(s, ver);
        }

        if (KnownPlugins.isKnown(query))
            return PluginResolver.asUrl(ghName, KnownPlugins.getKnown(query).url);
        else if (StringUtils.split("/", query).length == 2)
            return GitHubURLBuilder.urlValidate("https://github.com/" + s, ver);

        //configのorgを順番にfetch


        if (ghName instanceof String)
            if (GitHubURLBuilder.isRepoExists(ghName + "/" + query))
                return GitHubURLBuilder.urlValidate("https://github.com/" + ghName + "/" + s, ver);
            else
                return "ERROR " + query + "が見つかりませんでした。";

        if (ghName instanceof List)
        {
            for (String str : (List<String>) ghName)
                if (GitHubURLBuilder.isRepoExists(str + "/" + query))
                    return GitHubURLBuilder.urlValidate("https://github.com/" + str + "/" + s, ver);

        }
        else if (ghName instanceof String[])
            for (String str : (String[]) ghName)
                if (GitHubURLBuilder.isRepoExists(str + "/" + query))
                    return GitHubURLBuilder.urlValidate("https://github.com/" + str + "/" + s, ver);

        return "ERROR " + query + "が見つかりませんでした。";
    }

}
