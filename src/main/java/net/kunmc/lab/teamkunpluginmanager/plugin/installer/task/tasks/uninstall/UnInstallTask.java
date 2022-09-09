package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall;

import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.InstallTask;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.signals.PluginDisablingSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.signals.PluginRegisteredRecipeSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.signals.PluginUnloadingSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.loader.CommandsPatcher;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandleManager;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.PluginClassLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * プラグインをアンインストールするタスクです。
 */
public class UnInstallTask extends InstallTask<UnInstallArgument, UnInstallResult>
{
    private static final PluginManager PLUGIN_MANAGER;
    private static final CommandsPatcher COMMANDS_PATCHER;

    private static final List<Plugin> PLUGINS; // Lorg/bukkit/plugin/SimplePluginManager/plugins;
    private static final Map<String, Plugin> LOOKUP_NAMES; // Lorg/bukkit/plugin/SimplePluginManager/lookupNames;
    private static final Field F_PLUGIN; // Lorg/bukkit/plugin/JavaPlugin;
    private static final Field F_PLUGIN_INIT; // Lorg/bukkit/plugin/JavaPlugin;

    static
    {
        PLUGIN_MANAGER = Bukkit.getPluginManager();
        COMMANDS_PATCHER = new CommandsPatcher();

        try
        {
            Field fPlugins = SimplePluginManager.class.getDeclaredField("plugins");
            fPlugins.setAccessible(true);
            //noinspection unchecked
            PLUGINS = (List<Plugin>) fPlugins.get(PLUGIN_MANAGER);

            Field fLookupNames = SimplePluginManager.class.getDeclaredField("lookupNames");
            fLookupNames.setAccessible(true);
            //noinspection unchecked
            LOOKUP_NAMES = (Map<String, Plugin>) fLookupNames.get(PLUGIN_MANAGER);

            F_PLUGIN = PluginClassLoader.class.getDeclaredField("plugin");
            F_PLUGIN.setAccessible(true);

            F_PLUGIN_INIT = PluginClassLoader.class.getDeclaredField("pluginInit");
            F_PLUGIN_INIT.setAccessible(true);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    private UnInstallState taskState;

    public UnInstallTask(@NotNull InstallProgress<?> progress, @NotNull SignalHandleManager signalHandler)
    {
        super(progress, signalHandler);

        taskState = UnInstallState.INITIALIZED;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public @NotNull UnInstallResult runTask(@NotNull UnInstallArgument arguments)
    {
        List<Plugin> orderedUninstallTargets = arguments.getPlugins();

        List<PluginDescriptionFile> uninstalledPlugins = new ArrayList<>();
        Map<UnInstallErrorCause, PluginDescriptionFile> errors = new HashMap<>();

        this.taskState = UnInstallState.UNINSTALLING;

        for (Plugin plugin : orderedUninstallTargets)
        {
            PluginDescriptionFile description = plugin.getDescription();
            UnInstallErrorCause errorCause = uninstallOnePlugin(plugin);
            if (errorCause == UnInstallErrorCause.INTERNAL_UNINSTALL_OK)
                uninstalledPlugins.add(description);
            else
                errors.put(errorCause, description);
        }

        Runner.runLaterAsync(() -> {
            orderedUninstallTargets.forEach(plugin -> {
                File pluginFile = PluginUtil.getFile(plugin);
                if (pluginFile.exists())
                    pluginFile.delete();

                DependencyTree.wipePlugin(plugin);
            });
        }, 20L);

        orderedUninstallTargets.stream()
                .map(Plugin::getDescription)
                .forEach(UnInstallTask.this.progress::addRemoved);

        System.gc();

        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);

        boolean success = errors.isEmpty();

        return new UnInstallResult(success, taskState, success ? null: UnInstallErrorCause.SOME_UNINSTALL_FAILED,
                uninstalledPlugins, errors
        );
    }

    private UnInstallErrorCause uninstallOnePlugin(@NotNull Plugin plugin)
    {
        this.taskState = UnInstallState.RECIPES_UNREGISTERING;
        this.unregisterRecipes(plugin);

        this.taskState = UnInstallState.COMMANDS_UNPATCHING;
        COMMANDS_PATCHER.unPatchCommand(plugin, false);

        this.taskState = UnInstallState.PLUGIN_DISABLING;
        try
        {
            this.postSignal(new PluginDisablingSignal.Pre(plugin));
            PLUGIN_MANAGER.disablePlugin(plugin);
            this.postSignal(new PluginDisablingSignal.Post(plugin));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return UnInstallErrorCause.INTERNAL_PLUGIN_DISABLE_FAILED;
        }

        this.taskState = UnInstallState.REMOVING_FROM_BUKKIT;
        PLUGINS.remove(plugin);
        LOOKUP_NAMES.remove(plugin.getName());

        this.taskState = UnInstallState.CLASSES_UNLOADING;
        @SuppressWarnings("StringOperationCanBeSimplified")  // Backup Plugin name to unload classes
        String pluginName = new String(plugin.getName());

        this.postSignal(new PluginUnloadingSignal.Pre(plugin));

        if (!this.unloadClasses(plugin))
            return UnInstallErrorCause.INTERNAL_CLASS_UNLOAD_FAILED;

        this.postSignal(new PluginUnloadingSignal.Post(pluginName));

        return UnInstallErrorCause.INTERNAL_UNINSTALL_OK;
    }

    private boolean unloadClasses(@NotNull Plugin plugin)
    {
        ClassLoader classLoader = plugin.getClass().getClassLoader();

        if (!(classLoader instanceof URLClassLoader))
            return false;

        URLClassLoader urlClassLoader = (URLClassLoader) classLoader;

        try
        {
            F_PLUGIN.set(classLoader, null);
            F_PLUGIN_INIT.set(classLoader, null);

            urlClassLoader.close();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            TeamKunPluginManager.getPlugin().getLogger().warning("Unable to unload classes of plugin " + plugin.getName());
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            TeamKunPluginManager.getPlugin().getLogger().warning("Unable to close class loader of plugin " +
                    plugin.getName());
            return false;
        }

        return true;
    }

    private void unregisterRecipes(@NotNull Plugin plugin)
    {
        PluginRegisteredRecipeSignal.Searching signal = new PluginRegisteredRecipeSignal.Searching(plugin);
        this.postSignal(signal);
        String[] targetNamespaces = signal.getTargetNamespaces();

        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        while (recipeIterator.hasNext())
        {
            Recipe recipe = recipeIterator.next();

            if (this.isRecipeRemoveTarget(plugin, targetNamespaces, recipe))
                recipeIterator.remove();
        }
    }

    private boolean isRecipeRemoveTarget(@NotNull Plugin plugin, @NotNull String[] targetNamespaces, @NotNull Recipe recipe)
    {
        if (!(recipe instanceof Keyed))
            return false;

        NamespacedKey recipeKey = ((Keyed) recipe).getKey();
        String recipeNamespace = recipeKey.getNamespace();

        String foundSignature = Arrays.stream(targetNamespaces).parallel()
                .filter(recipeNamespace::equalsIgnoreCase)
                .findFirst().orElse(null);
        boolean isTargetRecipe = foundSignature != null;

        if (!isTargetRecipe)
            return false;

        PluginRegisteredRecipeSignal.FoundOne foundOneSignal =
                new PluginRegisteredRecipeSignal.FoundOne(plugin, foundSignature, recipe);
        this.postSignal(foundOneSignal);

        return foundOneSignal.isDoUnregister();
    }

}
