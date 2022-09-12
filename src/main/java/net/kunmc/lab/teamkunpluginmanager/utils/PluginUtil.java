package net.kunmc.lab.teamkunpluginmanager.utils;

import net.kunmc.lab.teamkunpluginmanager.plugin.InstallResult;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginUtil  // TODO: Rewrite this class
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
            throw new RuntimeException(e);
        }
    }

    public static String getPluginString(PluginDescriptionFile description)
    {
        return String.format("%s (%s)", description.getName(), description.getVersion());
    }

    public static String getPluginString(Plugin plugin)
    {
        return getPluginString(plugin.getDescription());
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> ms2Map(MemorySection ms)
    {
        try
        {
            Field field = MemorySection.class.getDeclaredField("map");
            field.setAccessible(true);

            LinkedHashMap<String, Object> obj = (LinkedHashMap<String, Object>) field.get(ms);

            //LinkedHashMap<String, Object> tmp = obj;

            obj.forEach((k, v) -> {
                if (v instanceof MemorySection)
                    obj.put(k, ms2Map((MemorySection) v));
            });

            return obj;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }

    public static Map<String, Object> getConfig(Plugin plugin)
    {
        if (plugin == null)
            return new LinkedHashMap<>();

        if (!new File(plugin.getDataFolder(), "config.yml").exists())
            return new LinkedHashMap<>();

        MemorySection section = plugin.getConfig();

        return ms2Map(section);
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

    public static String loadToString(PluginLoadOrder order)
    {
        switch (order)
        {
            case POSTWORLD:
                return "ワールド読み込み後に起動";
            case STARTUP:
                return "起動直後に読み込み";
            default:
                return "不明";
        }
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
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<InstallResult> mathLoadOrder(ArrayList<InstallResult> files)
    {
        ArrayList<InstallResult> order = new ArrayList<>(); //読み込む順番
        ArrayList<InstallResult> want = (ArrayList<InstallResult>) files.clone(); //処理待ち
        files.stream().parallel()
                .forEach(stringStringPair -> {
                    if (!want.contains(stringStringPair)) //既に処理されていた(処理待ちになかった)場合は無視
                        return;
                    if (!stringStringPair.isSuccess())
                        return;

                    PluginDescriptionFile desc; //dependとか
                    try
                    {
                        desc = loadDescription(new File("plugins/" + stringStringPair.getFileName())); //読み込み順番を取得
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        return;
                    }

                    desc.getDepend().stream().parallel()
                            .forEach(pluginName -> {
                                if (containValue(pluginName, want)) //dependに含まれていたものがインスコ対象ににあった
                                {
                                    order.add(getContainsEntry(pluginName, want)); //読み込み指示
                                    want.remove(getContainsEntry(pluginName, want)); //後始末
                                }
                            });

                    desc.getSoftDepend().stream().parallel()
                            .forEach(pluginName -> {
                                if (containValue(pluginName, want)) //softDependに含まれていたものがインスコ対象ににあった
                                {
                                    order.add(getContainsEntry(pluginName, want)); //読み込み指示
                                    want.remove(getContainsEntry(pluginName, want)); //後始末
                                }
                            });
                });

        want.forEach(stringStringPair -> {
            if (order.contains(stringStringPair.getFileName()))
                return;
            order.add(stringStringPair); //後回しプラグインの指示
        });
        return order;
    }

    private static InstallResult getContainsEntry(String contain, ArrayList<InstallResult> keys)
    {
        for (InstallResult p : keys)
        {
            if (p.getPluginName().equals(contain))
                return p;
        }
        return null;
    }

    private static boolean containValue(String contain, ArrayList<InstallResult> keys)
    {
        AtomicBoolean ab = new AtomicBoolean(false);
        keys.stream()
                .filter(stringStringPair -> stringStringPair.getPluginName().equals(contain))
                .forEach(stringStringPair -> ab.set(true));
        return ab.get();
    }

    public static PluginDescriptionFile loadDescription(File file) throws InvalidDescriptionException, IOException
    { // TODO: File -> Path
        if (!file.exists())
            throw new FileNotFoundException("Not found a file.");

        ZipFile zip = new ZipFile(file);
        ZipEntry entry = null;
        for (ZipEntry ent : Collections.list(zip.entries()))
            if (ent.getName().equals("plugin.yml"))
            {
                entry = ent;
                break;
            }

        if (entry == null)
            throw new InvalidDescriptionException("This file isn't plugin.");

        InputStream is = zip.getInputStream(entry);

        PluginDescriptionFile desc = new PluginDescriptionFile(is);
        is.close();
        zip.close();

        return desc;
    }
}
