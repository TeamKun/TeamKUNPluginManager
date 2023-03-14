package org.kunlab.kpm.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

@UtilityClass
public class PluginUtil
{

    private static final Method pluginGetFile;
    private static final int HASH_BUFFER_SIZE = 1024;

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
            throw new IllegalStateException(e);
        }
    }

    public static PluginDescriptionFile loadDescription(File file) throws InvalidDescriptionException, IOException
    { // TODO: File -> Path
        if (!file.exists())
            throw new FileNotFoundException("Not found the file.");

        try (ZipFile zip = new ZipFile(file))
        {
            ZipEntry entry = Collections.list(zip.entries()).stream()
                    .filter(ent -> ent.getName().equals("plugin.yml"))
                    .findFirst()
                    .orElse(null);

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

    @SuppressWarnings("StatementWithEmptyBody")
    public static String getHash(Path path, String algo)
    {
        MessageDigest md;
        try
        {
            md = MessageDigest.getInstance(algo);
        }
        catch (NoSuchAlgorithmException e)
        {
            return "<No such algorithm: " + algo + ">";
        }

        try (FileInputStream fis = new FileInputStream(path.toFile());
             DigestInputStream dis = new DigestInputStream(fis, md))
        {
            byte[] buffer = new byte[HASH_BUFFER_SIZE];
            while (dis.read(buffer) != -1)
            {
            }

            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest)
                sb.append(String.format("%02x", b));

            return sb.toString();
        }
        catch (IOException e)
        {
            return "<IOException thrown: " + e.getMessage() + ">";
        }
    }
}
