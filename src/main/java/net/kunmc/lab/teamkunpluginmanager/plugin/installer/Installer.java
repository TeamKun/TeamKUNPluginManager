package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.resolve.PluginResolvedSuccessfulSignal;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult;

import java.io.IOException;

public class Installer
{
    public static InstallResult installPlugin(String query, InstallerSignalHandler signalHandler)
    {
        PlumbingInstaller internal;
        InstallProgress progress;

        // region Initialize install. Phase: INITIALIZING
        try
        {
            internal = PlumbingInstaller.initInstall(signalHandler);
            progress = internal.getProgress();
        }
        catch (IOException | SecurityException e)
        {
            return InstallResult.error(InstallProgress.dummy(), FailedReason.IO_EXCEPTION_OCCURRED);
        }

        // endregion

        // region Query resolving. Phase: QUERY_RESOLVING, MULTIPLE_RESULT_RESOLVING => QUERY_RESOLVING
        progress.setPhase(InstallPhase.QUERY_RESOLVING);

        ResolveResult queryResolveResult = internal.resolvePlugin(query);
        queryResolveResult = internal.normalizeResolveResult(query, queryResolveResult);

        InstallResult mayQueryError = internal.checkResolveError(queryResolveResult);
        if (mayQueryError != null)
            return mayQueryError;

        assert queryResolveResult != null;  // if queryResolveResult is null, mayQueryError must not be null.

        signalHandler.handleSignal(new PluginResolvedSuccessfulSignal((SuccessResult) queryResolveResult));
        // endregion

        SuccessResult resolvedPlugin = (SuccessResult) queryResolveResult;

        // region Downloading. Phase: START_DOWNLOADING=>DOWNLOADING
        progress.setPhase(InstallPhase.START_DOWNLOADING);

        PlumbingInstaller.DownloadResult downloadResult = internal.downloadJar(progress, resolvedPlugin);

        if (!downloadResult.isSuccess())
            return InstallResult.error(progress, downloadResult.getDownloadFailedReason());


    }
}
