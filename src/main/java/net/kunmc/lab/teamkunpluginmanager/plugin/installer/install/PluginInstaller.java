package net.kunmc.lab.teamkunpluginmanager.plugin.installer.install;

import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.plugin.AbstractInstaller;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.install.signals.AlreadyInstalledPluginSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.DependencyElement;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.collector.DependsCollectArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.collector.DependsCollectPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.computer.DependsComputeOrderPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.computer.DependsComputeOrderResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.install.PluginsInstallArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.install.PluginsInstallPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolveArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolvePhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.assertion.IgnoredPluginSignal;
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

public class PluginInstaller extends AbstractInstaller<InstallErrorCause, InstallPhases>
{
    public PluginInstaller(@NotNull InstallerSignalHandler signalHandler) throws IOException
    {
        super(signalHandler);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public InstallResult<InstallPhases> execute(@NotNull String query)
    {
        Path pluginFilePath;
        PluginDescriptionFile pluginDescription;
        String pluginName;
        // region Do plugin resolve, download and description load.

        PhaseResult pluginDescriptionResult =
                this.submitter(InstallPhases.RESOLVING_QUERY, new PluginResolvePhase(progress, signalHandler))
                        .then(InstallPhases.DOWNLOADING, new DownloadPhase(progress, signalHandler))
                        .then(InstallPhases.LOADING_PLUGIN_DESCRIPTION, new DescriptionLoadPhase(progress, signalHandler))
                        .submit(new PluginResolveArgument(query));

        if (!pluginDescriptionResult.isSuccess())
            return handlePhaseError(pluginDescriptionResult);

        DescriptionLoadResult descriptionLoadResult = (DescriptionLoadResult) pluginDescriptionResult;

        pluginFilePath = descriptionLoadResult.getPluginFile();
        pluginDescription = descriptionLoadResult.getDescription();
        assert pluginDescription != null; // Not null because isSuccess() is true.

        pluginName = pluginDescription.getName();
        // endregion

        boolean replacePlugin = false;
        // region Do assertions.

        this.progress.setPhase(InstallPhases.CHECKING_ENVIRONMENT);

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
        PhaseResult dependsComputeOrderResult =
                this.submitter(InstallPhases.COLLECTING_DEPENDENCIES, new DependsCollectPhase(progress, signalHandler))
                        .then(InstallPhases.COMPUTING_LOAD_ORDER, new DependsComputeOrderPhase(progress, signalHandler))
                        .submit(new DependsCollectArgument(pluginDescription));

        if (!dependsComputeOrderResult.isSuccess())
            return handlePhaseError(dependsComputeOrderResult);

        dependenciesLoadOrder = ((DependsComputeOrderResult) dependsComputeOrderResult).getOrder();
        // endregion

        // region Install plugins.

        PhaseResult pluginsInstallResult =
                this.submitter(InstallPhases.INSTALLING_PLUGINS, new PluginsInstallPhase(progress, signalHandler))
                        .submit(new PluginsInstallArgument(
                                pluginFilePath, pluginDescription, dependenciesLoadOrder));

        if (!pluginsInstallResult.isSuccess())
            return handlePhaseError(pluginDescriptionResult);
        // endregion

        if (replacePlugin)
            this.progress.addUpgraded(pluginDescription);
        else
            this.progress.addInstalled(pluginDescription);

        return this.success();
    }

    private void removeOldPlugin(Plugin plugin)
    {
        this.progress.setPhase(InstallPhases.REMOVING_OLD_PLUGIN);

        File oldPluginFile = PluginUtil.getFile(plugin);

        PluginLoader.getInstance().unloadPlugin(plugin);  // TODO: Replace with uninstall.

        if (!safeDelete(oldPluginFile))
            Runner.runLater(() -> {
                safeDelete(oldPluginFile);
            }, 10L);
    }
}
