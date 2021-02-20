package net.kunmc.lab.teamkunpluginmanager.utils;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPlugins;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.w3c.dom.stylesheets.LinkStyle;

import javax.imageio.event.IIOReadProgressListener;
import java.lang.reflect.Array;
import java.util.List;

public class PluginResolver
{
    /**
     * プラグインのURLを名前解決します。
     * @param query 指定
     * @return URLまたはError
     */
    public static String asUrl(String query)
    {
        if (UrlValidator.getInstance().isValid(query))
            return GitHubURLBuilder.urlValidate(query);

        if (KnownPlugins.isKnown(query))
            return GitHubURLBuilder.urlValidate(KnownPlugins.getKnown(query).url);
        else if (StringUtils.split("/", query).length == 2)
            return GitHubURLBuilder.urlValidate("https://github.com/" + query);

        //configのorgを順番にfetch

        Object obj = TeamKunPluginManager.plugin.getConfig().get("gitHubName");

        if (obj instanceof String)
            if (GitHubURLBuilder.isRepoExists(obj + "/" + query))
                return GitHubURLBuilder.urlValidate("https://github.com/" + obj + "/" + query);
            else
                return "ERROR " + query + "が見つかりませんでした。";

        if (!(obj instanceof List) && !(obj instanceof String[]))
            return "ERROR " + query + "が見つかりませんでした。";


        for(String str: TeamKunPluginManager.config.getStringList("gitHubName"))
        {
            if (GitHubURLBuilder.isRepoExists(str + "/" + query))
                return GitHubURLBuilder.urlValidate("https://github.com/" + str + "/" + query);
        }

        return "ERROR " + query + "が見つかりませんでした。";
    }

}
