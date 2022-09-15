package net.kunmc.lab.teamkunpluginmanager.plugin.loader;

import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.PluginClassLoader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * プラグインを読み込むためのクラスです。
 */
public class PluginLoader
{
    private static PluginLoader INSTANCE;

    private final PluginManager pluginManager;
    private final CommandsPatcher commandsPatcher;

    private List<Plugin> plugins;
    private Map<String, Plugin> lookupNames;

    private Field fPlugin; // Lorg/bukkit/plugin/JavaPlugin;
    private Field fPluginInit; // Lorg/bukkit/plugin/JavaPlugin;

    public PluginLoader()
    {
        setInstance(this);

        this.pluginManager = Bukkit.getPluginManager();
        this.commandsPatcher = new CommandsPatcher();

        this.initReflections();
    }

    public static PluginLoader getInstance()
    {
        return INSTANCE;
    }

    private static void setInstance(@NotNull PluginLoader instance)
    {
        if (INSTANCE != null)
            throw new IllegalStateException("PluginLoader is already initialized.");

        INSTANCE = instance;
    }

    @SuppressWarnings("unchecked")
    private void initReflections()
    {
        try
        {
            Field fPlugins = this.pluginManager.getClass().getDeclaredField("plugins");
            fPlugins.setAccessible(true);
            this.plugins = (List<Plugin>) fPlugins.get(this.pluginManager);

            Field fLookupNames = this.pluginManager.getClass().getDeclaredField("lookupNames");
            fLookupNames.setAccessible(true);
            this.lookupNames = (Map<String, Plugin>) fLookupNames.get(this.pluginManager);

            Field fPlugin = PluginClassLoader.class.getDeclaredField("plugin");
            fPlugin.setAccessible(true);
            this.fPlugin = fPlugin;

            Field fPluginInit = PluginClassLoader.class.getDeclaredField("pluginInit");
            fPluginInit.setAccessible(true);
            this.fPluginInit = fPluginInit;
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * プラグインを読み込みます。
     *
     * @param pluginPath プラグインのパス
     * @return プラグインの読み込み結果
     */
    public PluginLoadResult loadPlugin(@NotNull Path pluginPath)
    {
        if (!pluginPath.toFile().exists())
            return PluginLoadResult.FILE_NOT_FOUND;

        Plugin plugin;
        try
        {
            plugin = Bukkit.getPluginManager().loadPlugin(pluginPath.toFile());
        }
        catch (InvalidPluginException e)
        {
            return PluginLoadResult.INVALID_PLUGIN_FILE.withException(e);
        }
        catch (InvalidDescriptionException e)
        {
            return PluginLoadResult.INVALID_PLUGIN_DESCRIPTION.withException(e);
        }
        catch (UnknownDependencyException e)
        {
            return PluginLoadResult.DEPENDENCY_NOT_FOUND.withException(e);
        }

        assert plugin != null;

        try
        {
            // Process Plugin loaded handler
            plugin.onLoad();
        }
        catch (Exception e)
        {
            return PluginLoadResult.EXCEPTION_ON_ONLOAD_HANDLING.withException(e);
        }

        Bukkit.getPluginManager().enablePlugin(plugin);

        if (!plugin.isEnabled())
            return PluginLoadResult.ENABLE_PLUGIN_FAILED;

        Runner.runLater(() -> this.commandsPatcher.patchCommand(plugin), 1L);

        return PluginLoadResult.OK;
    }

    /**
     * プラグインをアンロードします。
     *
     * @param plugin アンロードするプラグイン
     */
    public void unloadPlugin(@NotNull Plugin plugin)
    {
        this.getPluginRecipes(plugin.getName())
                .forEach(Bukkit::removeRecipe);

        this.commandsPatcher.unPatchCommand(plugin);

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.disablePlugin(plugin);

        this.removePluginCommands(plugin);

        this.plugins.remove(plugin);
        this.lookupNames.remove(plugin.getName());

        this.unloadClasses(plugin);
    }

    private void unloadClasses(Plugin plugin)
    {
        ClassLoader classLoader = plugin.getClass().getClassLoader();

        if (!(classLoader instanceof URLClassLoader))
            return;

        URLClassLoader urlClassLoader = (URLClassLoader) classLoader;

        try
        {
            this.fPlugin.set(classLoader, null);
            this.fPluginInit.set(classLoader, null);

            urlClassLoader.close();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            TeamKunPluginManager.getPlugin().getLogger().warning("Unable to unload classes of plugin " + plugin.getName());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            TeamKunPluginManager.getPlugin().getLogger().warning("Unable to close class loader of plugin " + plugin.getName());
        }

        // Check +XX:+DisableExplicitGC flag
        System.gc();
    }

    private void removePluginCommands(Plugin plugin)
    {
        Map<String, Command> knownCommands = this.commandsPatcher.getKnownCommands();
        Iterator<Map.Entry<String, Command>> it = knownCommands.entrySet().iterator();

        while (it.hasNext())
        {
            Map.Entry<String, Command> entry = it.next();
            if (entry.getValue() instanceof PluginCommand)
            {
                PluginCommand pluginCommand = (PluginCommand) entry.getValue();
                if (pluginCommand.getPlugin().getName().equalsIgnoreCase(plugin.getName()))
                {
                    pluginCommand.unregister(this.commandsPatcher.getCommandMap());
                    it.remove();
                }
                continue;
            }

            Field fPlugin = Arrays.stream(entry.getValue().getClass().getDeclaredFields()).parallel()
                    .filter(field -> field.getType().isAssignableFrom(Plugin.class))
                    .findFirst().orElse(null);
            if (fPlugin == null)
                continue;

            fPlugin.setAccessible(true);
            try
            {
                if (!((Plugin) fPlugin.get(entry.getValue())).getName().equalsIgnoreCase(plugin.getName()))
                    continue;

                entry.getValue().unregister(this.commandsPatcher.getCommandMap());
                it.remove();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            catch (IllegalStateException e)
            {
                if (!e.getMessage().equals("zip file closed"))
                {
                    e.printStackTrace();
                    continue;
                }

                entry.getValue().unregister(this.commandsPatcher.getCommandMap());
                it.remove();
            }
        }

        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    private List<NamespacedKey> getPluginRecipes(String pluginName)
    {
        List<NamespacedKey> result = new ArrayList<>();

        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while (iterator.hasNext())
        {
            Recipe recipe = iterator.next();
            if (recipe instanceof ShapedRecipe)
            {
                ShapedRecipe sr = (ShapedRecipe) recipe;
                if (sr.getKey().getNamespace().equals(pluginName.toLowerCase(Locale.ENGLISH)))
                    result.add(sr.getKey());
            }
        }

        return result;
    }

    /**
     * プラグインを再読み込みします。
     *
     * @param plugin 再読み込みするプラグイン
     */
    public void reloadPlugin(Plugin plugin)
    {
        Path pluginPath = PluginUtil.getFile(plugin).toPath();

        this.unloadPlugin(plugin);
        this.loadPlugin(pluginPath);
    }
}
