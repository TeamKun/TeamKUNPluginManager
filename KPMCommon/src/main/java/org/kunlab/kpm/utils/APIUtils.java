package org.kunlab.kpm.utils;

public class APIUtils
{
    public static String getAPIVersion()
    {
        String version = org.bukkit.Bukkit.getServer().getMinecraftVersion();
        String[] split = version.split("\\.");
        return split[0] + "." + split[1];
    }
}
