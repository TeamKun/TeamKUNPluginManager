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
        ArrayList<String> order = new ArrayList<>();
        AtomicReference<ArrayList<Pair<String, String>>> filesCopy = new AtomicReference<>(new ArrayList<>(files));
        AtomicBoolean ab = new AtomicBoolean(false);
        files.stream().parallel()
                .forEach(s -> {
                    String filename = "plugins/" + s.getKey();

                    PluginDescriptionFile desc;
                    try
                    {
                        desc = PluginUtil.loadDescription(new File(filename));
                    }
                    catch (InvalidDescriptionException | IOException e)
                    {
                        e.printStackTrace();
                        return;
                    }

                    desc.getDepend().forEach(s1 -> {
                        if (containValue(s1, files))
                        {
                            ab.set(true);
                            order.add(filename);
                            filesCopy.set(removeByValue(filesCopy.get(), s1));
                        }
                    });

                    if (ab.get())
                        return;

                    desc.getSoftDepend().forEach(s1 -> {
                        if (containValue(s1, files))
                        {
                            ab.set(true);
                            order.add(filename);
                            filesCopy.set(removeByValue(filesCopy.get(), s1));
                        }
                    });
                });
        filesCopy.get().stream().parallel()
                .forEach(foa -> {
                    order.add("plugins/" + foa.getKey());
                });
        return order;
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

        return desc;
    }

}
