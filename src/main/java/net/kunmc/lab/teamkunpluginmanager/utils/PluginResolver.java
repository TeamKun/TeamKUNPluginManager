package net.kunmc.lab.teamkunpluginmanager.utils;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.utils.rdmarker.DevBukkit;
import net.kunmc.lab.teamkunpluginmanager.utils.rdmarker.Spigotmc;
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
    public static String asUrl(String query)
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
            return PluginResolver.asUrl(KnownPlugins.getKnown(query).url);
        else if (StringUtils.split(query, "/").length == 2)
            return GitHubURLBuilder.urlValidate("https://github.com/" + s, ver);

        //configのorgを順番にfetch

        Object obj = TeamKunPluginManager.plugin.getConfig().get("gitHubName");

        if (obj instanceof String)
            if (GitHubURLBuilder.isRepoExists(obj + "/" + s))
                return GitHubURLBuilder.urlValidate("https://github.com/" + obj + "/" + s, ver);
            else
                return "ERROR " + query + "が見つかりませんでした。";

        if (!(obj instanceof List) && !(obj instanceof String[]))
            return "ERROR " + query + "が見つかりませんでした。";


        for (String str : TeamKunPluginManager.config.getStringList("gitHubName"))
        {
            if (GitHubURLBuilder.isRepoExists(str + "/" + query))
                return GitHubURLBuilder.urlValidate("https://github.com/" + str + "/" + s, ver);
        }

        return "ERROR " + query + "が見つかりませんでした。";
    }

}
