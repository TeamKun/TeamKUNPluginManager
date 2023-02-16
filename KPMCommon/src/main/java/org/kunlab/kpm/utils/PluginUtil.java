package org.kunlab.kpm.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

@UtilityClass
public class PluginUtil
{

    private static final Method pluginGetFile;

    static
    {
        try
        {
            pluginGetFile = ReflectionUtils.getAccessibleMethod(JavaPlugin.class, "getFile");
            pluginGetFile.setAccessible(true);
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public static boolean isPluginLoaded(String plugin)
    {
        if (Bukkit.getPluginManager().getPlugin(plugin) == null)
            return false;
        return isPluginLoaded(Bukkit.getPluginManager().getPlugin(plugin));
    }

    public static boolean isPluginLoaded(Plugin plugin)
    {
        if (plugin == null)
            return false;
        if (!(plugin.getClass().getClassLoader() instanceof PluginClassLoader))
            return false;
        return ((PluginClassLoader) plugin.getClass().getClassLoader()).getPlugin() != null;
    }

    public static File getFile(Plugin plugin)
    {
        try
        {
            return (File) pluginGetFile.invoke(plugin);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public static PluginDescriptionFile loadDescription(File file) throws InvalidDescriptionException, IOException
    { // TODO: File -> Path
        if (!file.exists())
            throw new FileNotFoundException("Not found the file.");

        try (ZipFile zip = new ZipFile(file))
        {
            ZipEntry entry = null;
            for (ZipEntry ent : Collections.list(zip.entries()))
                if (ent.getName().equals("plugin.yml"))
                {
                    entry = ent;
                    break;
                }

            if (entry == null)
                throw new InvalidDescriptionException("This file isn't plugin.");

            try (InputStream is = zip.getInputStream(entry))
            {
                return new PluginDescriptionFile(is);
            }
        }
        catch (ZipException ex)
        {
            throw new InvalidDescriptionException("This file isn't plugin.");
        }
    }

}
