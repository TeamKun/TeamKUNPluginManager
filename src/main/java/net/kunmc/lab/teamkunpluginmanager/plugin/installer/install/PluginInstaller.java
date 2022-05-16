package net.kunmc.lab.teamkunpluginmanager.plugin.installer.install;

import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.plugin.AbstractInstaller;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.DependsCollectArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.DependsCollectPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.DependsCollectResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadPhase;
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
import java.nio.file.Path;
import java.util.HashMap;

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
        {
            this.progress.setPhase(InstallPhases.REMOVING_OLD_PLUGIN);

            File oldPluginFile = PluginUtil.getFile(sameServerPlugin);

            PluginUtil.unload(sameServerPlugin);  // TODO: Replace with uninstall.

            if (!safeDelete(oldPluginFile))
                Runner.runLater(() -> {
                    safeDelete(oldPluginFile);
                }, 10L);
        }
        // endregion

        if (replacePlugin)
            this.progress.addUpgraded(pluginName);
        else
            this.progress.addInstalled(pluginName);

        HashMap<String, Path> collectedDependencies;
        // region Collecting dependencies section.
        this.progress.setPhase(InstallPhases.COLLECTING_DEPENDENCIES);

        DependsCollectResult dependsCollectResult = new DependsCollectPhase(progress, signalHandler)
                .runPhase(new DependsCollectArgument(pluginDescription));

        if (!dependsCollectResult.isSuccess())
            return this.error(InstallErrorCause.SOME_DEPENDENCY_COLLECT_FAILED);

        collectedDependencies = dependsCollectResult.getCollectedPlugins();
        //endregion


        return this.success();
    }
}