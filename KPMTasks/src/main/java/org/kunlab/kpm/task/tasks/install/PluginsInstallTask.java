package org.kunlab.kpm.task.tasks.install;

import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.hook.hooks.PluginInstalledHook;
import org.kunlab.kpm.installer.interfaces.Installer;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.kpminfo.KPMInformationFile;
import org.kunlab.kpm.meta.InstallOperator;
import org.kunlab.kpm.meta.interfaces.PluginMetaManager;
import org.kunlab.kpm.meta.interfaces.PluginMetaProvider;
import org.kunlab.kpm.task.AbstractInstallTask;
import org.kunlab.kpm.task.interfaces.dependencies.DependencyElement;
import org.kunlab.kpm.task.loader.CommandsPatcher;
import org.kunlab.kpm.task.tasks.install.signals.PluginEnablingSignal;
import org.kunlab.kpm.task.tasks.install.signals.PluginIncompatibleWithKPMSignal;
import org.kunlab.kpm.task.tasks.install.signals.PluginInstallingSignal;
import org.kunlab.kpm.task.tasks.install.signals.PluginLoadSignal;
import org.kunlab.kpm.task.tasks.install.signals.PluginOnLoadRunningSignal;
import org.kunlab.kpm.task.tasks.install.signals.PluginRelocatingSignal;
import org.kunlab.kpm.versioning.Version;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * プラグインをインストールするタスクです。
 */
public class PluginsInstallTask extends AbstractInstallTask<PluginsInstallArgument, PluginsInstallResult>
{
    private final Path pluginDir;
    private final PluginManager pluginManager;
    private final CommandsPatcher commandsPatcher;

    private final PluginMetaManager pluginMetaManager;
    private final PluginMetaProvider pluginMetaProvider;
    private PluginsInstallState state;

    public PluginsInstallTask(@NotNull Installer<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>> installer)
    {
        super(installer.getProgress(), installer.getProgress().getSignalHandler());

        KPMRegistry registry = installer.getRegistry();

        this.pluginDir = registry.getEnvironment().getDataDirPath().getParent();
        this.pluginManager = Bukkit.getPluginManager();
        this.commandsPatcher = new CommandsPatcher();

        PluginMetaManager pluginMetaManager = registry.getPluginMetaManager();
        this.pluginMetaManager = pluginMetaManager;
        this.pluginMetaProvider = pluginMetaManager.getProvider();

        this.state = PluginsInstallState.INITIALIZED;
    }

    private void patchPluginCommands(List<? extends Plugin> targets)
    {
        targets.forEach(plugin -> this.commandsPatcher.patchCommand(plugin, false));

        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    @Override
    public @NotNull PluginsInstallResult runTask(@NotNull PluginsInstallArgument arguments)
    {
        List<DependencyElement> dependencies = arguments.getDependencies();

        List<Plugin> installedPlugins = new ArrayList<>();

        // For commands patch
        try
        {
            // Install dependencies
            for (DependencyElement dependency : dependencies)
            {
                PluginDescriptionFile pluginDescription = dependency.getPluginDescription();
                Path path = dependency.getPluginPath();

                PluginsInstallResult result =
                        this.installOne(path, pluginDescription, dependency.getKpmInfoFile(),
                                installedPlugins, true, arguments.isOnlyLocate()
                        );
                if (!result.isSuccess())  // installOne returns null if installation is failed
                    return result;
            }

            for (Plugin plugin : installedPlugins)
                this.pluginMetaProvider.buildDependencyTree(plugin);

            if (arguments.getPluginPath() == null)  // Dependency only mode
                return new PluginsInstallResult(
                        true,
                        this.state,
                        null,
                        null,
                        null, installedPlugins
                );

            // Install plugin after dependencies installed

            assert arguments.getPluginDescription() != null;

            PluginsInstallResult result =
                    this.installOne(arguments.getPluginPath(), arguments.getPluginDescription(),
                            arguments.getKpmInformation(), installedPlugins, false, arguments.isOnlyLocate()
                    );

            if (result.isSuccess() && result.getInstalledPlugin() != null)
                this.pluginMetaProvider.buildDependencyTree(result.getInstalledPlugin());

            return result;
        }
        finally
        {
            Runner.runLater(() -> this.patchPluginCommands(installedPlugins), 1L);
        }
    }

    @NotNull
    private PluginsInstallResult installOne(@NotNull Path path, @NotNull PluginDescriptionFile pluginDescription,
                                            @Nullable KPMInformationFile kpmInformationFile,
                                            @NotNull List<Plugin> installedPlugins,
                                            boolean isDependency, boolean isOnlyLocate)
    {
        // Define variables
        InstallOperator operator = isDependency ? InstallOperator.KPM_DEPENDENCY_RESOLVER: InstallOperator.SERVER_ADMIN;
        String query;
        if (kpmInformationFile != null && kpmInformationFile.getUpdateQuery() != null)
            query = kpmInformationFile.getUpdateQuery().toString();
        else
            query = null;

        // Start install
        this.postSignal(new PluginInstallingSignal(path, pluginDescription));

        PluginsInstallErrorCause checkEnvError;
        if ((checkEnvError = this.checkEnv(pluginDescription, kpmInformationFile)) != null)
            return new PluginsInstallResult(false, this.state, checkEnvError);

        this.state = PluginsInstallState.PLUGIN_RELOCATING;

        // Relocate plugin
        String fileName = pluginDescription.getName() + "-" + pluginDescription.getVersion() + ".jar";
        Path targetPath = this.pluginDir.resolve(fileName);

        PluginsInstallResult mayError = this.movePlugin(path, targetPath);

        if (mayError != null)
            return mayError;

        if (isOnlyLocate)
            return new PluginsInstallResult(true,
                    this.state,
                    null,
                    null, null,
                    installedPlugins
            );

        KPMRegistry registry = this.progress.getInstaller().getRegistry();
        Plugin target;
        try
        {
            // Load plugin
            this.state = PluginsInstallState.PLUGIN_LOADING;
            this.postSignal(new PluginLoadSignal.Pre(path, pluginDescription));
            target = this.runSyncThrowing(() -> this.pluginManager.loadPlugin(targetPath.toFile()));
            this.postSignal(new PluginLoadSignal.Post(path, pluginDescription, target));

            this.progress.addPending(target.getName());

            // Run Plugin#onLoad
            this.state = PluginsInstallState.ONLOAD_RUNNING;
            this.postSignal(new PluginOnLoadRunningSignal.Pre(target));
            this.runSyncThrowing(target::onLoad);
            this.postSignal(new PluginOnLoadRunningSignal.Post(target));

        }
        catch (InvalidDescriptionException e)
        {
            registry.getExceptionHandler().report(e);
            return new PluginsInstallResult(false, this.state, PluginsInstallErrorCause.INVALID_PLUGIN_DESCRIPTION);
        }
        catch (InvalidPluginException e)
        {
            registry.getExceptionHandler().report(e);
            return new PluginsInstallResult(false, this.state, PluginsInstallErrorCause.INVALID_PLUGIN);
        }
        catch (Exception e)
        {
            registry.getExceptionHandler().report(e);
            return new PluginsInstallResult(false, this.state, PluginsInstallErrorCause.EXCEPTION_OCCURRED);
        }

        // Enable plugin
        this.pluginMetaManager.preparePluginModify(target);
        this.state = PluginsInstallState.PLUGIN_ENABLING;
        this.postSignal(new PluginEnablingSignal.Pre(target));
        this.runSync(() -> this.pluginManager.enablePlugin(target));
        if (!target.isEnabled())
        {
            this.postSignal(new PluginEnablingSignal.Failed(target));
            return new PluginsInstallResult(false, this.state, PluginsInstallErrorCause.EXCEPTION_OCCURRED);
        }

        this.postSignal(new PluginEnablingSignal.Post(target));

        installedPlugins.add(target);

        this.pluginMetaManager.onInstalled(
                target,
                operator,
                query,
                isDependency
        );

        if (kpmInformationFile != null)
            this.runSync(() ->
                    kpmInformationFile.getHooks().runHook(new PluginInstalledHook(
                            operator,
                            isDependency,
                            query
                    ))
            );

        return new PluginsInstallResult(true, this.state, null, null, target, installedPlugins);
    }

    @Nullable
    private String generateSHA1(@NotNull Path path) throws IOException
    {
        try (FileInputStream inputStream = new FileInputStream(path.toFile()))
        {
            return DigestUtils.sha1Hex(inputStream);
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
    }

    private boolean moveFile(@NotNull Path source, @NotNull Path target, boolean overwrite) throws IOException
    {
        try
        {
            if (overwrite)
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            else
                Files.move(source, target);

            return true;
        }
        catch (FileAlreadyExistsException e)
        {
            String sourceHash = this.generateSHA1(source);
            String targetHash = this.generateSHA1(target);

            if (sourceHash == null || targetHash == null)
                return false;

            if (sourceHash.equals(targetHash))
                return true;
            else
                return this.moveFile(source, target, true);
        }
    }

    @Nullable
    private PluginsInstallResult movePlugin(@NotNull Path source, @NotNull Path target)
    {
        KPMRegistry registry = this.progress.getInstaller().getRegistry();

        PluginRelocatingSignal signal = new PluginRelocatingSignal(source, target);
        this.postSignal(signal);

        Path actualTarget = signal.getTarget(); // May be changed by signal

        try
        {
            if (!this.moveFile(source, actualTarget, false))
                return new PluginsInstallResult(false, this.state, PluginsInstallErrorCause.RELOCATE_FAILED);
            else
                return null;
        }
        catch (IOException e)
        {
            registry.getExceptionHandler().report(e);
            return new PluginsInstallResult(false, this.state, PluginsInstallErrorCause.IO_EXCEPTION_OCCURRED);
        }
        catch (Exception e)
        {
            registry.getExceptionHandler().report(e);
            return new PluginsInstallResult(false, this.state, PluginsInstallErrorCause.RELOCATE_FAILED);
        }
    }

    @Nullable
    @SuppressWarnings("deprecation")
    private PluginsInstallErrorCause checkEnv(PluginDescriptionFile pluginDescription, @Nullable KPMInformationFile kpmInformation)
    {

        if (pluginDescription.getAPIVersion() != null)
        {
            String apiVersion = pluginDescription.getAPIVersion();
            if (!Bukkit.getUnsafe().isSupportedApiVersion(apiVersion))
                return PluginsInstallErrorCause.INCOMPATIBLE_WITH_KPM_VERSION;
        }

        if (kpmInformation == null)
            return null;

        Version daemonVersion = this.progress.getInstaller().getRegistry().getVersion();

        if (kpmInformation.getKpmVersion().isNewerThan(daemonVersion))
        {
            PluginIncompatibleWithKPMSignal incompatibleSignal =
                    new PluginIncompatibleWithKPMSignal(pluginDescription, kpmInformation, daemonVersion);
            this.postSignal(incompatibleSignal);

            if (!incompatibleSignal.isForceInstall())
                return PluginsInstallErrorCause.INCOMPATIBLE_WITH_KPM_VERSION;

        }

        return null;
    }
}
