package net.kunmc.lab.kpm.task.tasks.download;

import net.kunmc.lab.kpm.http.DownloadProgress;
import net.kunmc.lab.kpm.http.RequestMethod;
import net.kunmc.lab.kpm.http.Requests;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;
import net.kunmc.lab.kpm.interfaces.installer.PluginInstaller;
import net.kunmc.lab.kpm.task.AbstractInstallTask;
import net.kunmc.lab.kpm.task.tasks.download.signals.DownloadErrorSignal;
import net.kunmc.lab.kpm.task.tasks.download.signals.DownloadProgressSignal;
import net.kunmc.lab.kpm.task.tasks.download.signals.DownloadStartingSignal;
import net.kunmc.lab.kpm.task.tasks.download.signals.DownloadSucceedSignal;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

/**
 * ファイルをダウンロードするタスクです。
 * プラグイン以外のファイルにも使用できます。
 */
public class DownloadTask extends AbstractInstallTask<DownloadArgument, DownloadResult>
{
    private final String randomDownloadID;

    private DownloadState taskState;

    public DownloadTask(@NotNull PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>> installer)
    {
        super(installer.getProgress(), installer.getProgress().getSignalHandler());

        this.randomDownloadID = UUID.randomUUID().toString();

        this.taskState = DownloadState.INITIALIZED;
    }

    private void onDownload(DownloadProgress downloadProgress, String url)
    {
        this.taskState = DownloadState.DOWNLOADING;

        this.postSignal(new DownloadProgressSignal(
                this.randomDownloadID,
                url,
                downloadProgress.getTotalSize(),
                downloadProgress.getDownloaded(),
                downloadProgress.getPercentage()
        ));
    }

    @Override
    public @NotNull DownloadResult runTask(@NotNull DownloadArgument arguments)
    {
        Path downloadPath = arguments.getPath();
        if (downloadPath == null)
            downloadPath = this.progress.getInstallTempDir().resolve(this.randomDownloadID + ".kpmcache");

        DownloadStartingSignal downloadingSignal = new DownloadStartingSignal(
                this.randomDownloadID,
                downloadPath,
                arguments.getUrl()
        );

        this.taskState = DownloadState.START_DOWNLOADING;
        this.postSignal(downloadingSignal);  // SignalHandler can change the download URL and download path.

        Path path = downloadingSignal.getDownloadPath();
        String url = downloadingSignal.getUrl();

        try
        {
            long size = Requests.downloadFile(RequestMethod.GET, url, path, progress -> this.onDownload(progress, url));

            this.postSignal(new DownloadSucceedSignal(this.randomDownloadID, path, size));

            return new DownloadResult(true, this.taskState, url, path, size, this.randomDownloadID);
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

            DownloadErrorSignal error = new DownloadErrorSignal(this.randomDownloadID, url, cause, signalValue);
            this.postSignal(error);

            return new DownloadResult(false, this.taskState, url, path, -1, this.randomDownloadID, cause);
        }
        catch (Exception e)
        {
            this.postSignal(new DownloadErrorSignal(this.randomDownloadID, url,
                    DownloadErrorCause.UNKNOWN_ERROR, e
            ));

            return new DownloadResult(false, this.taskState, url, path, -1, this.randomDownloadID,
                    DownloadErrorCause.UNKNOWN_ERROR
            );
        }
    }
}
