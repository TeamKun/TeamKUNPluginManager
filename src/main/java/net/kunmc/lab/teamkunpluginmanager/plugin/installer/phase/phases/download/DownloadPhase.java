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
import java.util.UUID;

public class DownloadPhase extends InstallPhase<DownloadArgument, DownloadResult>
{
    private final String randomDownloadID;

    private DownloadState phaseState;

    public DownloadPhase(@NotNull InstallProgress<?> progress, @NotNull InstallerSignalHandler signalHandler)
    {
        super(progress, signalHandler);

        this.randomDownloadID = UUID.randomUUID().toString();

        this.phaseState = DownloadState.INITIALIZED;
    }

    private void onDownload(DownloadProgress downloadProgress)
    {
        this.phaseState = DownloadState.DOWNLOADING;

        this.postSignal(new DownloadProgressSignal(
                this.randomDownloadID,
                downloadProgress.getTotalSize(),
                downloadProgress.getDownloaded(),
                downloadProgress.getPercentage()
        ));
    }

    @Override
    public @NotNull DownloadResult runPhase(@NotNull DownloadArgument arguments)
    {
        Path downloadPath = arguments.getPath();
        if (downloadPath == null)
            downloadPath = progress.getInstallTempDir().resolve(this.randomDownloadID + ".kpmcache");

        DownloadStartingSignal downloadingSignal = new DownloadStartingSignal(
                this.randomDownloadID,
                downloadPath,
                arguments.getUrl()
        );

        this.phaseState = DownloadState.START_DOWNLOADING;
        this.postSignal(downloadingSignal);  // SignalHandler can change the download URL and download path.

        Path path = downloadingSignal.getDownloadPath();
        String url = downloadingSignal.getUrl();

        try
        {
            long size = Requests.downloadFile(RequestMethod.GET, url, path, this::onDownload);

            this.postSignal(new DownloadSucceedSignal(this.randomDownloadID, path, size));

            return new DownloadResult(true, this.phaseState, path, size, this.randomDownloadID);
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

            DownloadErrorSignal error = new DownloadErrorSignal(cause, this.randomDownloadID, signalValue);
            this.postSignal(error);

            return new DownloadResult(false, this.phaseState, path, -1, this.randomDownloadID, cause);
        }
        catch (Exception e)
        {
            this.postSignal(new DownloadErrorSignal(DownloadErrorCause.UNKNOWN_ERROR, this.randomDownloadID, e));

            return new DownloadResult(false, this.phaseState, path, -1, this.randomDownloadID,
                    DownloadErrorCause.UNKNOWN_ERROR
            );
        }
    }
}
