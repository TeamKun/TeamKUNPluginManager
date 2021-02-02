package net.kunmc.lab.teamkunpluginmanager.utils;

import javafx.util.Pair;
import org.bukkit.Bukkit;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginUtil
{
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

    public static String getFileSizeString(long bytes)
    {
        String suffix = "B";

        BigDecimal dec = new BigDecimal(String.valueOf(bytes));
        BigDecimal div = new BigDecimal(1024);
        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "KiB";
        }

        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "MiB";
        }

        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "GiB";
        }

        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "TiB";
        }


        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "PiB";
        }


        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "EiB";
        }


        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "ZiB";
        }


        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "YiB";
        }

        dec = dec.setScale(3, BigDecimal.ROUND_HALF_UP);


        return new DecimalFormat("#,###.##;#,###.##").format(dec) + suffix;
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
        Method getFileMethod;
        try
        {
            getFileMethod = JavaPlugin.class.getDeclaredMethod("getFile");
            getFileMethod.setAccessible(true);

            return (File) getFileMethod.invoke(plugin);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<String> mathLoadOrder(ArrayList<Pair<String, String>> files)
    {
        ArrayList<String> order = new ArrayList<>(); //読み込む順番
        ArrayList<Pair<String, String>> want = (ArrayList<Pair<String, String>>) files.clone(); //処理待ち
        files.stream().parallel()
                .forEach(stringStringPair -> {
                    if (!want.contains(stringStringPair)) //既に処理されていた(処理待ちになかった)場合は無視
                        return;
                    PluginDescriptionFile desc; //dependとか
                    try
                    {
                        desc = loadDescription(new File("plugins/" + stringStringPair.getKey())); //読み込み順番を取得
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
                                    order.add(getContainsEntry(pluginName, want).getKey()); //読み込み指示
                                    want.remove(getContainsEntry(pluginName, want)); //後始末
                                }
                            });

                    desc.getSoftDepend().stream().parallel()
                            .forEach(pluginName -> {
                                if (containValue(pluginName, want)) //softDependに含まれていたものがインスコ対象ににあった
                                {
                                    order.add(getContainsEntry(pluginName, want).getKey()); //読み込み指示
                                    want.remove(getContainsEntry(pluginName, want)); //後始末
                                }
                            });
                });

        want.forEach(stringStringPair -> {
            if (order.contains(stringStringPair.getKey()))
                return;
            order.add(stringStringPair.getKey()); //後回しプラグインの指示
        });
        return order;
    }

    private static Pair<String, String> getContainsEntry(String contain, ArrayList<Pair<String, String>> keys)
    {
        AtomicReference<Pair<String, String>> ab = new AtomicReference<>();
        keys.stream()
                .filter(stringStringPair -> stringStringPair.getValue().equals(contain))
                .forEach(ab::set);
        return ab.get();
    }

    private static boolean containValue(String contain, ArrayList<Pair<String, String>> keys)
    {
        AtomicBoolean ab = new AtomicBoolean(false);
        keys.stream()
                .filter(stringStringPair -> stringStringPair.getValue().equals(contain))
                .forEach(stringStringPair -> ab.set(true));
        return ab.get();
    }

    private static ArrayList<Pair<String, String>> removeByValue(ArrayList<Pair<String, String>> original, String value)
    {
        ArrayList<Pair<String, String>> copyOf = (ArrayList<Pair<String, String>>) original.clone();

        original.stream().parallel()
                .forEach(stringStringPair -> {
                    if (stringStringPair.getValue().equals(value))
                        copyOf.remove(stringStringPair);
                });

        return copyOf;
    }

    public static PluginDescriptionFile loadDescription(File file) throws InvalidDescriptionException, IOException
    {
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
