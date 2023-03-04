package org.kunlab.kpm.task.tasks.uninstall;

import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.PluginClassLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.ExceptionHandler;
import org.kunlab.kpm.hook.hooks.PluginUninstallHook;
import org.kunlab.kpm.hook.hooks.RecipesUnregisteringHook;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;
import org.kunlab.kpm.installer.interfaces.PluginInstaller;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.kpminfo.KPMInformationFile;
import org.kunlab.kpm.task.AbstractInstallTask;
import org.kunlab.kpm.task.CommandsPatcher;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginDisablingSignal;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginIsDependencySignal;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginRegisteredRecipeSignal;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginUninstallErrorSignal;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginUninstallingSignal;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginUnloadingSignal;
import org.kunlab.kpm.task.tasks.uninstall.signals.StartingGCSignal;
import org.kunlab.kpm.utils.PluginUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * プラグインをアンインストールするタスクです。
 */
public class UnInstallTask extends AbstractInstallTask<UninstallArgument, UnInstallResult>
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
            throw new IllegalStateException(e);
        }
    }

    private final KPMRegistry registry;
    private final ExceptionHandler exceptionHandler;
    private final List<PluginDescriptionFile> uninstalledPlugins;
    private final List<PluginDescriptionFile> disabledDependencyPlugins;
    private final Map<PluginDescriptionFile, Path> unloadedPlugins;

    private UninstallState taskState;

    public UnInstallTask(@NotNull PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>> installer)
    {
        super(installer.getProgress(), installer.getProgress().getSignalHandler());

        this.registry = installer.getRegistry();
        this.exceptionHandler = this.registry.getExceptionHandler();
        this.uninstalledPlugins = new ArrayList<>();
        this.disabledDependencyPlugins = new ArrayList<>();
        this.unloadedPlugins = new HashMap<>();

        this.taskState = UninstallState.INITIALIZED;
    }

    private static boolean isDependencyModeOf(@Nullable PluginIsDependencySignal.Operation operation,
                                              @NotNull PluginIsDependencySignal.Operation excepted,
                                              @NotNull List<String> dependencies,
                                              @NotNull Plugin plugin)
    {
        return operation == excepted && dependencies.stream().parallel().anyMatch(plugin.getName()::equalsIgnoreCase);
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public @NotNull UnInstallResult runTask(@NotNull UninstallArgument arguments)
    {
        List<Plugin> orderedUninstallTargets = arguments.getPlugins();
        List<String> argDependencies =
                arguments.getDependencies() == null ? Collections.emptyList(): arguments.getDependencies();

        Map<UninstallErrorCause, PluginDescriptionFile> errors = new HashMap<>();

        this.taskState = UninstallState.UNINSTALLING;

        for (Plugin plugin : orderedUninstallTargets)
        {
            boolean isDepIgnore = isDependencyModeOf(arguments.getOperation(),
                    PluginIsDependencySignal.Operation.IGNORE, argDependencies, plugin
            );
            boolean isDepDisable = isDependencyModeOf(arguments.getOperation(),
                    PluginIsDependencySignal.Operation.DISABLE, argDependencies, plugin
            );
            boolean disableOnly = isDepDisable || arguments.isDisableOnly();

            if (isDepIgnore)
                continue;  // In dependency ignore mode, skip any plugin operation. (The signal explicitly allows the dependency error.)

            this.registry.getPluginMetaManager().preparePluginModify(plugin.getName());

            PluginDescriptionFile description = plugin.getDescription();
            UninstallErrorCause errorCause = this.uninstallOnePlugin(plugin, disableOnly);

            if (errorCause == UninstallErrorCause.INTERNAL_UNINSTALL_OK)
                this.uninstalledPlugins.add(description);
            else if (errorCause == UninstallErrorCause.INTERNAL_DISABLE_AND_UNINSTALL_OK)
                this.disabledDependencyPlugins.add(description);
            else
            {
                this.postSignal(new PluginUninstallErrorSignal(errorCause, description));
                errors.put(errorCause, description);
            }
        }

        Runner.runLaterAsync(() -> orderedUninstallTargets.forEach(plugin -> {
            boolean isDepOnlyUnload = isDependencyModeOf(arguments.getOperation(),
                    PluginIsDependencySignal.Operation.UNLOAD_ONLY, argDependencies, plugin
            );
            boolean isFileDel = !isDepOnlyUnload && arguments.isDeleteFile();

            File pluginFile = PluginUtil.getFile(plugin);
            if (isFileDel && pluginFile.exists())
                pluginFile.delete();
        }), 20L);

        orderedUninstallTargets.stream()
                .map(Plugin::getDescription)
                .forEach(UnInstallTask.this.progress::addRemoved);

        if (arguments.isRunGC())
        {
            this.postSignal(new StartingGCSignal());
            System.gc();
        }

        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);

        boolean success = errors.isEmpty();

        return new UnInstallResult(success, this.taskState, success ? null: UninstallErrorCause.SOME_UNINSTALL_FAILED,
                this.uninstalledPlugins, this.disabledDependencyPlugins, this.unloadedPlugins,
                errors
        );
    }

    private void removePluginCommands(Plugin plugin)
    {
        Map<String, Command> knownCommands = COMMANDS_PATCHER.getKnownCommands();
        Iterator<Map.Entry<String, Command>> it = knownCommands.entrySet().iterator();

        while (it.hasNext())
        {
            Map.Entry<String, Command> entry = it.next();
            if (entry.getValue() instanceof PluginCommand)
            {
                PluginCommand pluginCommand = (PluginCommand) entry.getValue();
                if (pluginCommand.getPlugin().getName().equalsIgnoreCase(plugin.getName()))
                {
                    pluginCommand.unregister(COMMANDS_PATCHER.getCommandMap());
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

                entry.getValue().unregister(COMMANDS_PATCHER.getCommandMap());
                it.remove();
            }
            catch (IllegalAccessException e)
            {
                this.exceptionHandler.report(e);
            }
            catch (IllegalStateException e)
            {
                if (!e.getMessage().equals("zip file closed"))
                {
                    this.exceptionHandler.report(e);
                    continue;
                }

                entry.getValue().unregister(COMMANDS_PATCHER.getCommandMap());
                it.remove();
            }
        }
    }

    private UninstallErrorCause disableOnePlugin(@NotNull Plugin plugin, @Nullable KPMInformationFile kpmInfo)
    {
        this.taskState = UninstallState.RECIPES_UNREGISTERING;
        this.unregisterRecipes(plugin, kpmInfo);

        this.taskState = UninstallState.COMMANDS_UNPATCHING;
        COMMANDS_PATCHER.unPatchCommand(plugin, false);
        this.removePluginCommands(plugin);

        this.taskState = UninstallState.PLUGIN_DISABLING;
        try
        {
            this.postSignal(new PluginDisablingSignal.Pre(plugin));
            PLUGIN_MANAGER.disablePlugin(plugin);
            this.postSignal(new PluginDisablingSignal.Post(plugin));
        }
        catch (Exception ex)
        {
            this.exceptionHandler.report(ex);
            return UninstallErrorCause.INTERNAL_PLUGIN_DISABLE_FAILED;
        }

        this.disabledDependencyPlugins.add(plugin.getDescription());

        return UninstallErrorCause.INTERNAL_UNINSTALL_OK;
    }

    private UninstallErrorCause unloadOnePlugin(@NotNull Plugin plugin)
    {
        this.taskState = UninstallState.REMOVING_FROM_BUKKIT;
        PLUGINS.remove(plugin);
        LOOKUP_NAMES.remove(plugin.getName().toLowerCase(Locale.ENGLISH));

        this.taskState = UninstallState.CLASSES_UNLOADING;
        @SuppressWarnings("StringOperationCanBeSimplified")  // Backup Plugin name to unload classes
        String pluginName = new String(plugin.getName());

        this.postSignal(new PluginUnloadingSignal.Pre(plugin));

        if (!this.unloadClasses(plugin))
            return UninstallErrorCause.INTERNAL_CLASS_UNLOAD_FAILED;

        this.unloadedPlugins.put(plugin.getDescription(), PluginUtil.getFile(plugin).toPath());

        this.postSignal(new PluginUnloadingSignal.Post(pluginName));

        return UninstallErrorCause.INTERNAL_UNINSTALL_OK;
    }

    private UninstallErrorCause uninstallOnePlugin(@NotNull Plugin plugin, boolean onlyDisableMode)
    {
        this.postSignal(new PluginUninstallingSignal(plugin));

        KPMInformationFile kpmInfo;
        if (this.registry.getKpmInfoManager().hasInfo(plugin))
            kpmInfo = this.registry.getKpmInfoManager().getOrLoadInfo(plugin);
        else
            kpmInfo = null;

        if (kpmInfo != null)
            kpmInfo.getHooks().runHook(new PluginUninstallHook.Pre(plugin.getDescription(), kpmInfo, plugin));

        UninstallErrorCause mayError = this.runSync(() -> this.disableOnePlugin(plugin, kpmInfo));
        if (mayError != UninstallErrorCause.INTERNAL_UNINSTALL_OK)
            return mayError;

        if (onlyDisableMode)
            return UninstallErrorCause.INTERNAL_DISABLE_AND_UNINSTALL_OK;

        mayError = this.runSync(() -> this.unloadOnePlugin(plugin));
        if (mayError != UninstallErrorCause.INTERNAL_UNINSTALL_OK)
            return mayError;

        if (kpmInfo != null)
            this.runSync(() -> kpmInfo.getHooks().runHook(new PluginUninstallHook.Post(plugin.getDescription(), kpmInfo)));
        this.registry.getPluginMetaManager().onUninstalled(plugin.getName());
        return UninstallErrorCause.INTERNAL_UNINSTALL_OK;
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
            this.exceptionHandler.report(e);
            this.registry.getLogger().warning("Unable to unload classes of plugin " + plugin.getName());
            return false;
        }
        catch (IOException e)
        {
            this.exceptionHandler.report(e);
            this.registry.getLogger().warning("Unable to close class loader of plugin " +
                    plugin.getName());
            return false;
        }

        return true;
    }

    private void unregisterRecipes(@NotNull Plugin plugin, @Nullable KPMInformationFile kpmInfo)
    {
        List<String> targetNamespaces =
                new ArrayList<>(Collections.singletonList(plugin.getName().toLowerCase(Locale.ROOT)));
        if (kpmInfo != null)
        {
            targetNamespaces.addAll(Arrays.asList(kpmInfo.getRecipes()));

            RecipesUnregisteringHook.Searching searchingHook = new RecipesUnregisteringHook.Searching(targetNamespaces);
            kpmInfo.getHooks().runHook(searchingHook);
            targetNamespaces = searchingHook.getTargetNamespaces();
        }

        PluginRegisteredRecipeSignal.Searching signal =
                new PluginRegisteredRecipeSignal.Searching(plugin, targetNamespaces.toArray(new String[0]));
        this.postSignal(signal);

        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        while (recipeIterator.hasNext())
        {
            Recipe recipe = recipeIterator.next();

            if (this.isRecipeRemoveTarget(plugin, targetNamespaces, recipe))
            {
                if (kpmInfo != null)
                {
                    RecipesUnregisteringHook.Pre preHook = new RecipesUnregisteringHook.Pre(recipe);
                    kpmInfo.getHooks().runHook(preHook);
                    if (preHook.isCancelled())
                        continue;
                }

                this.postSignal(new PluginRegisteredRecipeSignal.Removing(plugin, recipe));
                recipeIterator.remove();

                if (kpmInfo != null)
                {
                    RecipesUnregisteringHook.Post postHook = new RecipesUnregisteringHook.Post(recipe);
                    kpmInfo.getHooks().runHook(postHook);
                }
            }
        }
    }

    private boolean isRecipeRemoveTarget(@NotNull Plugin plugin, @NotNull Collection<String> targetNamespaces, @NotNull Recipe recipe)
    {
        if (!(recipe instanceof Keyed))
            return false;

        NamespacedKey recipeKey = ((Keyed) recipe).getKey();
        String targetNS = recipeKey.getNamespace().toLowerCase(Locale.ROOT);
        String targetKey = recipeKey.getKey().toLowerCase(Locale.ROOT);
        String targetFullName = targetNS + ":" + targetKey;

        String foundSignature = targetNamespaces.stream().parallel()
                .filter(key ->
                        key.equalsIgnoreCase(targetNS)
                                || key.equalsIgnoreCase(targetKey)
                                || key.equalsIgnoreCase(targetFullName)
                )
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
