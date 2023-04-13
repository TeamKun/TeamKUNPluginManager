package org.kunlab.kpm.signal.handlers.common;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Progressbar;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.Utils;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.task.tasks.download.signals.DownloadErrorSignal;
import org.kunlab.kpm.task.tasks.download.signals.DownloadProgressSignal;
import org.kunlab.kpm.task.tasks.download.signals.DownloadSucceedSignal;

/**
 * ダウンロードのシグナルを処理するハンドラです.
 */
public class DownloadingSignalHandler
{
    private final Terminal terminal;
    private String currentDownload;

    // Ignore DownloadStartingSignal
    private long downloadTotalSize;
    private long downloadStarted;
    private Progressbar downloadProgressBar;

    public DownloadingSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    private void startDownloads(String id)
    {
        this.currentDownload = id;
        this.downloadTotalSize = 0;
        this.downloadStarted = System.currentTimeMillis();
        if (this.terminal.isPlayer())
        {
            this.downloadProgressBar = this.terminal.createProgressbar(
                    LangProvider.get("tasks.download.progress_bar")
            );
            this.downloadProgressBar.setProgressMax(100);
        }
        else
            this.downloadProgressBar = null;
    }

    private void addDownloadArtifact(String url, long size)
    {
        this.downloadTotalSize += size;
        this.terminal.infoImplicit(LangProvider.get(
                "tasks.download.get",
                MsgArgs.of("url", url)
                        .add("size", Utils.roundSizeUnit(size))
        ));
    }

    private void endDownloads()
    {
        long elapsedMillis = System.currentTimeMillis() - this.downloadStarted;
        long elapsedSec = elapsedMillis / 1000;
        if (elapsedSec == 0)
            elapsedSec = 1;
        long bytesPerSec = this.downloadTotalSize / elapsedSec;
        this.terminal.success(LangProvider.get(
                "tasks.download.success",
                MsgArgs.of("totalSize", Utils.roundSizeUnit(this.downloadTotalSize))
                        .add("time", elapsedSec)
                        .add("speed", Utils.roundSizeUnit(bytesPerSec))
        ));

        this.currentDownload = null;

        if (this.downloadProgressBar != null)
            this.terminal.removeProgressbar(LangProvider.get("tasks.download"));
    }

    @SignalHandler
    public void onDownloadingSignal(DownloadProgressSignal signal)
    {
        if (this.currentDownload == null)
        {
            this.startDownloads(signal.getDownloadId());
            this.addDownloadArtifact(signal.getUrl(), signal.getTotalSize());
        }

        if (this.downloadProgressBar == null)
            return;

        double percent = (double) signal.getDownloaded() / signal.getTotalSize();
        this.downloadProgressBar.setProgress(Double.valueOf(percent * 100).intValue());  // max 100
    }

    @SignalHandler
    public void onDownloadFailed(DownloadErrorSignal signal)
    {
        this.terminal.errorImplicit(LangProvider.get(
                "tasks.download.failed",
                MsgArgs.of("url", signal.getUrl())
                        .add("cause", signal.getCause())
                        .add("value", signal.getValue())
        ));
    }

    @SignalHandler
    public void onDownloadFinished(DownloadSucceedSignal signal)
    {
        this.endDownloads();
    }
}
