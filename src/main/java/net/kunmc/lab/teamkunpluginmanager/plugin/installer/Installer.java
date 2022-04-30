package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.GeneralPhaseErrorCause;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseEnum;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolveArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolvePhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolveResult;

import java.io.IOException;

public class Installer
{
    private static <T extends Enum<T> & PhaseEnum> InstallResult handlePhaseError(InstallProgress progress, PhaseResult<?, T> result)
    {
        if (result.getErrorCause() != null)
            return InstallResult.error(progress, result.getErrorCause(), result.getPhase());
        else
            return InstallResult.error(progress, GeneralPhaseErrorCause.ILLEGAL_INTERNAL_STATE, result.getPhase());
    }

    public static InstallResult installPlugin(String query, InstallerSignalHandler signalHandler)
    {
        InstallProgress progress;

        // region Initialize install. Phase: INITIALIZING
        try
        {
            progress = new InstallProgress(true);
            progress.setPhase(InstallPhases.INITIALIZING);
        }
        catch (IOException | SecurityException e)
        {
            return InstallResult.error(InstallProgress.dummy(), GeneralPhaseErrorCause.IO_EXCEPTION_OCCURRED);
        }

        // endregion

        // region Query resolving. Phase: QUERY_RESOLVE
        progress.setPhase(InstallPhases.QUERY_RESOLVING);

        PluginResolvePhase resolvePhase = new PluginResolvePhase(progress, signalHandler);
        PluginResolveResult resolveResult = resolvePhase.runPhase(new PluginResolveArgument(query));

        if (!resolveResult.isSuccess() || resolveResult.getResolveResult() == null)  // getResolveResult() == null is never true.
            return handlePhaseError(progress, resolveResult);
        // endregion

        // region Downloading. Phase: START_DOWNLOADING=>DOWNLOADING
        progress.setPhase(InstallPhases.DOWNLOADING);

        DownloadPhase downloadPhase = new DownloadPhase(progress, signalHandler);
        DownloadResult downloadResult = downloadPhase.runPhase(DownloadArgument.of(resolveResult));

        if (!downloadResult.isSuccess())
            return handlePhaseError(progress, downloadResult);

        // endregion

        // region Load plugin.yml. Phase: LOADING_PLUGIN_DESCRIPTION
        progress.setPhase(InstallPhases.LOADING_PLUGIN_DESCRIPTION);

        DescriptionLoadPhase descriptionLoadPhase = new DescriptionLoadPhase(progress, signalHandler);
        DescriptionLoadResult descriptionLoadResult =
                descriptionLoadPhase.runPhase(DescriptionLoadArgument.of(downloadResult));

        if (!downloadResult.isSuccess())
            return handlePhaseError(progress, downloadResult);
        // endregion

        // region Check the plugin is already installed. Phase: CHECK_INSTALLED
    }
}
