package net.kunmc.lab.teamkunpluginmanager.utils;

import javafx.util.Pair;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginUtil
{
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

    private static ArrayList<Pair<String, String>> removeByValue(ArrayList<Pair<String,String>> original, String value)
    {
        ArrayList<Pair<String, String>> copyOf = (ArrayList<Pair<String, String>>) original.clone();

        original.stream().parallel()
                .forEach(stringStringPair -> {
                    if (stringStringPair.getValue().equals(value))
                        copyOf.remove(stringStringPair);
                });

        return copyOf;
    }

    public static PluginDescriptionFile loadDescription(File file) throws InvalidDescriptionException, FileNotFoundException, IOException
    {
        if (!file.exists())
            throw new FileNotFoundException("Not found a file.");

        ZipFile zip = new ZipFile(file);
        ZipEntry entry = null;
        for (ZipEntry ent: Collections.list(zip.entries()))
            if (ent.getName().equals("plugin.yml"))
            {
                entry = ent;
                break;
            }

        if (entry == null)
            throw new InvalidDescriptionException("This file isn't plugin.");

        InputStream is = zip.getInputStream(entry);

        PluginDescriptionFile desc =  new PluginDescriptionFile(is);
        is.close();
        zip.close();

        return desc;
    }

}
