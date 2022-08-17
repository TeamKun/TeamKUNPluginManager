package net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.install;

import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.plugin.AbstractInstaller;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.install.signals.AlreadyInstalledPluginSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.assertion.IgnoredPluginSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.DependencyElement;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.DependsCollectArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.DependsCollectTask;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.computer.DependsComputeOrderArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.computer.DependsComputeOrderResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.computer.DependsComputeOrderTask;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.description.DescriptionLoadArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.description.DescriptionLoadResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.description.DescriptionLoadTask;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.DownloadArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.DownloadTask;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install.PluginsInstallArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install.PluginsInstallTask;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve.PluginResolveArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve.PluginResolveTask;
import net.kunmc.lab.teamkunpluginmanager.plugin.loader.PluginLoader;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class PluginInstaller extends AbstractInstaller<InstallErrorCause, InstallTasks>
{
    public PluginInstaller(@NotNull InstallerSignalHandler signalHandler) throws IOException
    {
        super(signalHandler);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public InstallResult<InstallTasks> execute(@NotNull String query)
    {
        Path pluginFilePath;
        PluginDescriptionFile pluginDescription;
        String pluginName;
        // region Do plugin resolve, download and description load.

        TaskResult pluginDescriptionResult =
                this.submitter(InstallTasks.RESOLVING_QUERY, new PluginResolveTask(progress, signalHandler))
                        .then(InstallTasks.DOWNLOADING, new DownloadTask(progress, signalHandler))
                        .bridgeArgument(result -> {
                            if (result.getResolveResult() == null)
                                throw new IllegalArgumentException("Plugin Resolving must be successful");

                            return new DownloadArgument(result.getResolveResult().getDownloadUrl());
                        })
                        .then(InstallTasks.LOADING_PLUGIN_DESCRIPTION, new DescriptionLoadTask(progress, signalHandler))
                        .bridgeArgument(result -> {
                            if (result.getPath() == null)
                                throw new IllegalArgumentException("Plugin Description Loading must be successful");

                            return new DescriptionLoadArgument(result.getPath());
                        })
                        .submitAll(new PluginResolveArgument(query));

        if (!pluginDescriptionResult.isSuccess())
            return handleTaskError(pluginDescriptionResult);

        DescriptionLoadResult descriptionLoadResult = (DescriptionLoadResult) pluginDescriptionResult;

        pluginFilePath = descriptionLoadResult.getPluginFile();
        pluginDescription = descriptionLoadResult.getDescription();
        assert pluginDescription != null; // Not null because isSuccess() is true.

        pluginName = pluginDescription.getName();
        // endregion

        boolean replacePlugin = false;
        // region Do assertions.

        this.progress.setCurrentTask(InstallTasks.CHECKING_ENVIRONMENT);

        // region Check if plugin is ignored.
        if (this.isPluginIgnored(pluginName))
        {
            IgnoredPluginSignal ignoredPluginSignal = new IgnoredPluginSignal(pluginDescription);
            this.postSignal(ignoredPluginSignal);

            if (ignoredPluginSignal.isCancelInstall())
                return this.error(InstallErrorCause.PLUGIN_IGNORED);
        }
        // endregion

        // region Check if plugin is already installed.

        Plugin sameServerPlugin = Bukkit.getPluginManager().getPlugin(pluginName);

        if (sameServerPlugin != null)
        {
            AlreadyInstalledPluginSignal alreadyInstalledPluginSignal =
                    new AlreadyInstalledPluginSignal(sameServerPlugin.getDescription(), pluginDescription);

            this.postSignal(alreadyInstalledPluginSignal);
            replacePlugin = alreadyInstalledPluginSignal.isReplacePlugin();

            if (!replacePlugin)
                return this.error(InstallErrorCause.PLUGIN_ALREADY_INSTALLED);
        }

        // endregion

        // endregion

        // region Remove plugin if it is already installed. (only replacePlugin is true)
        if (replacePlugin)
            this.removeOldPlugin(sameServerPlugin);
        // endregion

        this.progress.addPending(pluginDescription.getName());


        List<DependencyElement> dependenciesLoadOrder;
        // region Do collect dependencies, compute dependencies load order and install them.
        TaskResult dependsComputeOrderResult =
                this.submitter(InstallTasks.COLLECTING_DEPENDENCIES, new DependsCollectTask(progress, signalHandler))
                        .then(InstallTasks.COMPUTING_LOAD_ORDER, new DependsComputeOrderTask(progress, signalHandler))
                        .bridgeArgument(result -> new DependsComputeOrderArgument(result.getCollectedPlugins()))
                        .submitAll(new DependsCollectArgument(pluginDescription));

        if (!dependsComputeOrderResult.isSuccess())
            return handleTaskError(dependsComputeOrderResult);

        dependenciesLoadOrder = ((DependsComputeOrderResult) dependsComputeOrderResult).getOrder();
        // endregion

        // region Install plugins.

        TaskResult pluginsInstallResult =
                this.submitter(InstallTasks.INSTALLING_PLUGINS, new PluginsInstallTask(progress, signalHandler))
                        .submitAll(new PluginsInstallArgument(
                                pluginFilePath, pluginDescription, dependenciesLoadOrder));

        if (!pluginsInstallResult.isSuccess())
            return handleTaskError(pluginDescriptionResult);
        // endregion

        if (replacePlugin)
            this.progress.addUpgraded(pluginDescription);
        else
            this.progress.addInstalled(pluginDescription);

        return this.success();
    }

    private void removeOldPlugin(Plugin plugin)
    {
        this.progress.setCurrentTask(InstallTasks.REMOVING_OLD_PLUGIN);

        File oldPluginFile = PluginUtil.getFile(plugin);

        PluginLoader.getInstance().unloadPlugin(plugin);  // TODO: Replace with uninstall.

        if (!safeDelete(oldPluginFile))
            Runner.runLater(() -> {
                safeDelete(oldPluginFile);
            }, 10L);
    }
}
