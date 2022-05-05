package net.kunmc.lab.teamkunpluginmanager.plugin.installer.install;

import net.kunmc.lab.teamkunpluginmanager.plugin.AbstractInstaller;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolveArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolvePhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.assertion.AlreadyInstalledPluginSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.assertion.IgnoredPluginSignal;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

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


    }
}
