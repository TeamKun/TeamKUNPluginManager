package net.kunmc.lab.teamkunpluginmanager.plugin.installer.install;

import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.plugin.AbstractInstaller;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.collector.DependsCollectArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.collector.DependsCollectPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.computer.DependsComputeOrderPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.install.PluginsInstallPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.install.PluginsInstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolveArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolvePhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.assertion.AlreadyInstalledPluginSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.assertion.IgnoredPluginSignal;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class PluginInstaller extends AbstractInstaller<InstallErrorCause, InstallPhases>
{
    public PluginInstaller(@NotNull InstallerSignalHandler signalHandler) throws IOException
    {
        super(signalHandler);
    }

    @Override
    public InstallResult<InstallPhases> execute(@NotNull String query)
    {
        PluginDescriptionFile pluginDescription;
        String pluginName;
        // region Do plugin resolve, download and description load.

        DescriptionLoadResult pluginDescriptionResult = (DescriptionLoadResult)
                this.submitter(InstallPhases.RESOLVING_QUERY, new PluginResolvePhase(progress, signalHandler))
                        .then(InstallPhases.DOWNLOADING, new DownloadPhase(progress, signalHandler))
                        .then(InstallPhases.LOADING_PLUGIN_DESCRIPTION, new DescriptionLoadPhase(progress, signalHandler))
                        .submit(new PluginResolveArgument(query));

        if (!pluginDescriptionResult.isSuccess())
            return handlePhaseError(pluginDescriptionResult);

        pluginDescription = pluginDescriptionResult.getDescription();
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

        if (replacePlugin)
            this.progress.addUpgraded(pluginDescription);
        else
            this.progress.addInstalled(pluginDescription);

        // region Do collect dependencies, compute dependencies load order and install them.
        PluginsInstallResult pluginsInstallResult = (PluginsInstallResult)
                this.submitter(InstallPhases.COLLECTING_DEPENDENCIES, new DependsCollectPhase(progress, signalHandler))
                        .then(InstallPhases.COMPUTING_LOAD_ORDER, new DependsComputeOrderPhase(progress, signalHandler))
                        .then(InstallPhases.INSTALLING_PLUGINS, new PluginsInstallPhase(progress, signalHandler))
                        .submit(new DependsCollectArgument(pluginDescription));

        if (!pluginsInstallResult.isSuccess())
            return handlePhaseError(pluginDescriptionResult);
        // endregion


        return this.success();
    }

    private void removeOldPlugin(Plugin plugin)
    {
        this.progress.setPhase(InstallPhases.REMOVING_OLD_PLUGIN);

        File oldPluginFile = PluginUtil.getFile(plugin);

        PluginUtil.unload(plugin);  // TODO: Replace with uninstall.

        if (!safeDelete(oldPluginFile))
            Runner.runLater(() -> {
                safeDelete(oldPluginFile);
            }, 10L);
    }
}
