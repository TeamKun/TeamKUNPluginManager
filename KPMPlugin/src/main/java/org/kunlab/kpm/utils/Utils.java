package org.kunlab.kpm.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

@UtilityClass
public class Utils
{
    public static String getPluginString(Plugin plugin)
    {
        return getPluginString(plugin.getDescription());
    }

    public static String getPluginString(PluginDescriptionFile description)
    {
        return String.format("%s (%s)", description.getName(), description.getVersion());
    }
}
