package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.InstallPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.signals.DownloadErrorSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.signals.DownloadProgressSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.signals.DownloadStartingSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.signals.DownloadSucceedSignal;
import net.kunmc.lab.teamkunpluginmanager.utils.http.DownloadProgress;
import net.kunmc.lab.teamkunpluginmanager.utils.http.RequestMethod;
import net.kunmc.lab.teamkunpluginmanager.utils.http.Requests;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class DownloadPhase extends InstallPhase<DownloadArgument, DownloadResult>
{
    private DownloadState phaseState;

    public DownloadPhase(@NotNull InstallProgress progress, @NotNull InstallerSignalHandler signalHandler)
    {
        super(progress, signalHandler);

        this.phaseState = DownloadState.INITIALIZED;
    }

    private void onDownload(DownloadProgress downloadProgress)
    {
        this.phaseState = DownloadState.DOWNLOADING;

        this.postSignal(new DownloadProgressSignal(
                downloadProgress.getTotalSize(),
                downloadProgress.getDownloaded(),
                downloadProgress.getPercentage()
        ));
    }

    @Override
    public @NotNull DownloadResult runPhase(@NotNull DownloadArgument arguments)
    {
        DownloadStartingSignal downloadingSignal = new DownloadStartingSignal(
                progress.getInstallTempDir().resolve(progress.getInstallActionID().toString() + ".jar"),
                arguments.getUrl()
        );

        this.phaseState = DownloadState.START_DOWNLOADING;
        this.postSignal(downloadingSignal);  // SignalHandler can change the download URL and download path.

        Path path = downloadingSignal.getDownloadPath();
        String url = downloadingSignal.getUrl();

        try
        {
            long size = Requests.downloadFile(RequestMethod.GET, url, path, this::onDownload);

            this.postSignal(new DownloadSucceedSignal(path, size));

            return new DownloadResult(true, this.phaseState, path, size);
        }
        catch (IOException e)
        {
            DownloadErrorCause cause;
            Object signalValue;

            if (e.getMessage().startsWith("HTTP error "))
            {
                cause = DownloadErrorCause.ILLEGAL_HTTP_RESPONSE;
                signalValue = e.getMessage().substring(11);
            }
            else if (e.getMessage().startsWith("No response body was returned"))
            {
                cause = DownloadErrorCause.NO_BODY_IN_RESPONSE;
                signalValue = null;
            }
            else
            {
                cause = DownloadErrorCause.IO_EXCEPTION;
                signalValue = e;
            }

            DownloadErrorSignal error = new DownloadErrorSignal(cause, signalValue);
            this.postSignal(error);

            return new DownloadResult(true, this.phaseState, path, -1, cause);
        }
        catch (Exception e)
        {
            this.postSignal(new DownloadErrorSignal(DownloadErrorCause.UNKNOWN_ERROR, e));

            return new DownloadResult(true, this.phaseState, path, -1, DownloadErrorCause.UNKNOWN_ERROR);
        }
    }
}
