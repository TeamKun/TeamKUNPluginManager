package net.kunmc.lab.teamkunpluginmanager.utils;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.InstallResult;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLClassLoader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginUtil
{
    private static Method getCommandMap;
    private static Object craftServer;
    private static Object minecraftServer;
    private static Object commandDispatcher;

    static
    {
        try
        {
            craftServer = ReflectionUtils.PackageType.CRAFTBUKKIT.getClass("CraftServer").cast(Bukkit.getServer());
            minecraftServer = ReflectionUtils.getMethod(craftServer.getClass(), "getServer").invoke(craftServer);

            getCommandMap = ReflectionUtils.getMethod(craftServer.getClass(), "getCommandMap");
            commandDispatcher = ReflectionUtils.getMethod(minecraftServer.getClass(), "getCommandDispatcher")
                    .invoke(minecraftServer);
        }
        catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
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

    private static ArrayList<Pair<String, String>> removeByValue(ArrayList<Pair<String, String>> original, String value)
    {
        ArrayList<Pair<String, String>> copyOf = (ArrayList<Pair<String, String>>) original.clone();

        original.stream().parallel()
                .forEach(stringStringPair -> {
                    if (stringStringPair.getRight().equals(value))
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

    /**
     * Unload a plugin.
     *
     * @param plugin the plugin to unload
     * @author https://dev.bukkit.org/projects/plugman
     */
    @SuppressWarnings("unchecked")
    public static void unload(Plugin plugin)
    {
        getKnownCommands().entrySet().stream().parallel()
                .filter(stringCommandEntry -> stringCommandEntry.getValue() instanceof PluginIdentifiableCommand)
                .filter(stringCommandEntry -> {
                    PluginIdentifiableCommand command = (PluginIdentifiableCommand) stringCommandEntry.getValue();
                    return command.getPlugin().getName().equalsIgnoreCase(plugin.getName());
                })
                .forEach(stringCommandEntry -> {
                    unWrapCommand(stringCommandEntry.getKey());
                });

        getPluginRecipes(plugin.getName()).forEach(Bukkit::removeRecipe);

        Bukkit.getOnlinePlayers().stream().parallel().forEach(Player::updateCommands);

        String name = plugin.getName();

        PluginManager pluginManager = Bukkit.getPluginManager();

        SimpleCommandMap commandMap;

        List<Plugin> plugins;

        Map<String, Plugin> names;
        Map<String, Command> commands;
        Map<Event, SortedSet<RegisteredListener>> listeners = null;

        pluginManager.disablePlugin(plugin);

        try
        {

            Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
            pluginsField.setAccessible(true);
            plugins = (List<Plugin>) pluginsField.get(pluginManager);

            Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);
            names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

            try
            {
                Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
                listenersField.setAccessible(true);
                listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(pluginManager);
            }
            catch (Exception ignored)
            {
            }

            Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            commands = (Map<String, Command>) knownCommandsField.get(commandMap);

        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
            return;
        }

        pluginManager.disablePlugin(plugin);

        if (plugins != null)
            plugins.remove(plugin);

        if (names != null)
            names.remove(name);

        if (listeners != null)
        {
            for (SortedSet<RegisteredListener> set : listeners.values())
            {
                set.removeIf(value -> value.getPlugin() == plugin);
            }
        }

        if (commandMap != null)
        {
            for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry<String, Command> entry = it.next();
                if (entry.getValue() instanceof PluginCommand)
                {
                    PluginCommand c = (PluginCommand) entry.getValue();
                    if (c.getPlugin() == plugin)
                    {
                        c.setTabCompleter((a, b, c1, d) -> null);
                        c.unregister(commandMap);
                        it.remove();
                    }
                }
            }
        }

        // Attempt to close the classloader to unlock any handles on the plugin's jar file.
        ClassLoader cl = plugin.getClass().getClassLoader();

        if (cl instanceof URLClassLoader)
        {

            try
            {

                Field pluginField = cl.getClass().getDeclaredField("plugin");
                pluginField.setAccessible(true);
                pluginField.set(cl, null);

                Field pluginInitField = cl.getClass().getDeclaredField("pluginInit");
                pluginInitField.setAccessible(true);
                pluginInitField.set(cl, null);

            }
            catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ignored)
            {
            }

            try
            {

                ((URLClassLoader) cl).close();
            }
            catch (IOException ignored)
            {
            }

        }

        // Will not work on processes started with the -XX:+DisableExplicitGC flag, but lets try it anyway.
        // This tries to get around the issue where Windows refuses to unlock jar files that were previously loaded into the JVM.
        System.gc();
    }

    private static void load(Plugin plugin)
    {
        load(plugin.getName());
    }

    public static void load(String name)
    {
        File pluginDir = new File("plugins");
        if (!pluginDir.isDirectory())
        {
            return;
        }
        File pluginFile = new File(pluginDir, name + ".jar");
        if (!pluginFile.isFile())
        {
            File[] listFiles = pluginDir.listFiles();
            if (listFiles == null)
                return;
            int length = listFiles.length;
            int i = 0;
            while (true)
            {
                if (i < length)
                {
                    File f = listFiles[i];
                    if (f.getName().endsWith(".jar"))
                    {
                        try
                        {
                            if (TeamKunPluginManager.plugin.getPluginLoader().getPluginDescription(f).getName().equalsIgnoreCase(name))
                            {
                                pluginFile = f;
                                break;
                            }
                        }
                        catch (InvalidDescriptionException ignored)
                        {
                            return;
                        }
                    }
                    i++;
                }
                else
                    break;
            }
        }


        File finalPluginFile = pluginFile;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Plugin target = Bukkit.getPluginManager().loadPlugin(finalPluginFile);
                    Objects.requireNonNull(target).onLoad();
                    Bukkit.getPluginManager().enablePlugin(target);
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            getKnownCommands().entrySet().stream().parallel()
                                    .filter(stringCommandEntry -> stringCommandEntry.getValue() instanceof PluginIdentifiableCommand)
                                    .forEach(stringCommandEntry -> {
                                        wrapCommand(stringCommandEntry.getValue(), stringCommandEntry.getKey());
                                    });


                            Bukkit.getOnlinePlayers().stream().parallel().forEach(Player::updateCommands);
                        }
                    }.runTaskLater(TeamKunPluginManager.plugin, 10L);
                }
                catch (InvalidDescriptionException | InvalidPluginException e2)
                {
                    e2.printStackTrace();
                }
            }
        }.runTask(TeamKunPluginManager.plugin);

    }

    public static void reload(Plugin plugin)
    {
        unload(plugin);
        load(plugin);
    }

    public static Map<String, Command> getKnownCommands()
    {
        try
        {
            SimpleCommandMap commandMap =
                    (SimpleCommandMap) getCommandMap.invoke(craftServer);
            return commandMap.getKnownCommands();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public static List<NamespacedKey> getPluginRecipes(String pluginName)
    {
        List<NamespacedKey> result = new ArrayList<>();
        Recipe recipe;
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while (iterator.hasNext())
        {
            recipe = iterator.next();
            if (recipe instanceof ShapedRecipe)
            {
                ShapedRecipe sr = (ShapedRecipe) recipe;
                if (sr.getKey().getNamespace().equals(pluginName.toLowerCase(Locale.ROOT)))
                    result.add(sr.getKey());
            }
        }

        return result;
    }

    public static void wrapCommand(Command command, String alias)
    {
        try
        {
            Class<?> bukkitCommandWrapper = ReflectionUtils.PackageType.CRAFTBUKKIT_COMMAND.getClass("BukkitCommandWrapper");
            Object commandWrapper = bukkitCommandWrapper
                    .getConstructor(craftServer.getClass(), Command.class)
                    .newInstance(craftServer, command);

            Method bukkitCommandWrapperRegister = ReflectionUtils.getMethod(
                    bukkitCommandWrapper,
                    "register",
                    com.mojang.brigadier.CommandDispatcher.class,
                    String.class
            );

            Method a = ReflectionUtils.getMethod(ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("CommandDispatcher"), "a");

            bukkitCommandWrapperRegister.invoke(commandWrapper, a.invoke(commandDispatcher), alias);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    public static void unWrapCommand(String command)
    {

        try
        {
            Field b = ReflectionUtils.getField(commandDispatcher.getClass(), true, "b");
            ((com.mojang.brigadier.CommandDispatcher) b.get(commandDispatcher))
                    .getRoot().removeCommand(command);
        }
        catch (IllegalAccessException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }

    }

}
