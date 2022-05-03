package net.kunmc.lab.teamkunpluginmanager.plugin.installer.install;

import net.kunmc.lab.teamkunpluginmanager.plugin.AbstractInstaller;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolveArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolvePhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolveResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PluginInstaller extends AbstractInstaller<InstallErrorCause, InstallPhase>
{
    public PluginInstaller(@NotNull InstallerSignalHandler signalHandler) throws IOException
    {
        super(signalHandler);
    }

    public InstallResult<InstallPhase> execute(@NotNull String query)
    {
        // region Query resolving. Phase: QUERY_RESOLVE
        this.progress.setPhase(InstallPhase.QUERY_RESOLVING);

        PluginResolvePhase resolvePhase = new PluginResolvePhase(progress, signalHandler);
        PluginResolveResult resolveResult = resolvePhase.runPhase(new PluginResolveArgument(query));

        if (!resolveResult.isSuccess() || resolveResult.getResolveResult() == null)
            // getResolveResult() == null is never true.
            return handlePhaseError(resolveResult);
        // endregion

        // region Downloading. Phase: DOWNLOADING
        this.progress.setPhase(InstallPhase.DOWNLOADING);

        DownloadPhase downloadPhase = new DownloadPhase(progress, signalHandler);
        DownloadResult downloadResult = downloadPhase.runPhase(new DownloadArgument(resolveResult));

        if (!downloadResult.isSuccess())
            return handlePhaseError(downloadResult);

        // endregion

        // region Load plugin.yml. Phase: LOADING_PLUGIN_DESCRIPTION
        progress.setPhase(InstallPhase.LOADING_PLUGIN_DESCRIPTION);

        DescriptionLoadPhase descriptionLoadPhase = new DescriptionLoadPhase(progress, signalHandler);
        DescriptionLoadResult descriptionLoadResult =
                descriptionLoadPhase.runPhase(new DescriptionLoadArgument(downloadResult));

        if (!downloadResult.isSuccess())
            return handlePhaseError(downloadResult);
        // endregion

        // region Check the plugin is already installed. Phase: CHECK_INSTALLED
    }
}
