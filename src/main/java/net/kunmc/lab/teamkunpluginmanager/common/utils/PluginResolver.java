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
        if (UrlValidator.getInstance().isValid(query))
        {
            if (DevBukkit.isMatch(query))
                return DevBukkit.toDownloadUrl(query);
            else if (Spigotmc.isMatch(query))
                return Spigotmc.toDownloadUrl(query);
            return GitHubURLBuilder.urlValidate(query);
        }

        if (KnownPlugins.isKnown(query))
            return GitHubURLBuilder.urlValidate(KnownPlugins.getKnown(query).url);
        else if (StringUtils.split("/", query).length == 2)
            return GitHubURLBuilder.urlValidate("https://github.com/" + query);

        //configのorgを順番にfetch


        if (ghName instanceof String)
            if (GitHubURLBuilder.isRepoExists(ghName + "/" + query))
                return GitHubURLBuilder.urlValidate("https://github.com/" + ghName + "/" + query);
            else
                return "ERROR " + query + "が見つかりませんでした。";

        if (ghName instanceof List)
        {
            for (String str : (List<String>) ghName)
            {
                if (GitHubURLBuilder.isRepoExists(str + "/" + query))
                    return GitHubURLBuilder.urlValidate("https://github.com/" + str + "/" + query);
            }

        }
        else if (ghName instanceof String[])
        {
            for (String str : (String[]) ghName)
            {
                if (GitHubURLBuilder.isRepoExists(str + "/" + query))
                    return GitHubURLBuilder.urlValidate("https://github.com/" + str + "/" + query);
            }
        }

        return "ERROR " + query + "が見つかりませんでした。";
    }

}
