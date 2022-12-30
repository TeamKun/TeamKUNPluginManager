package net.kunmc.lab.kpm.signal.handlers.common;

import net.kunmc.lab.kpm.Utils;
import net.kunmc.lab.kpm.installer.task.tasks.download.signals.DownloadErrorSignal;
import net.kunmc.lab.kpm.installer.task.tasks.download.signals.DownloadProgressSignal;
import net.kunmc.lab.kpm.installer.task.tasks.download.signals.DownloadSucceedSignal;
import net.kunmc.lab.kpm.signal.SignalHandler;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Progressbar;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;

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
            this.downloadProgressBar = this.terminal.createProgressbar("ダウンロード");
            this.downloadProgressBar.setProgressMax(100);
        }
        else
            this.downloadProgressBar = null;
    }

    private void addDownloadArtifact(String url, long size)
    {
        this.downloadTotalSize += size;
        this.terminal.infoImplicit(
                "取得 %s [%s]",
                url,
                Utils.roundSizeUnit(size)
        );
    }

    private void endDownloads()
    {
        long elapsedMillis = System.currentTimeMillis() - this.downloadStarted;
        long elapsedSec = elapsedMillis / 1000;
        if (elapsedSec == 0)
            elapsedSec = 1;
        long bytesPerSec = this.downloadTotalSize / elapsedSec;
        this.terminal.success(
                "%s を " + ChatColor.YELLOW + "%d秒" + ChatColor.RESET + "で取得しました (" +
                        ChatColor.YELLOW + "%s/s" + ChatColor.RESET + ")",
                Utils.roundSizeUnit(this.downloadTotalSize),
                elapsedSec,
                Utils.roundSizeUnit(bytesPerSec)
        );

        this.currentDownload = null;

        if (this.downloadProgressBar != null)
            this.terminal.removeProgressbar("ダウンロード");
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
        this.downloadProgressBar.setProgress((int) (percent * 100));  // max 100
    }

    @SignalHandler
    public void onDownloadFailed(DownloadErrorSignal signal)
    {
        this.terminal.errorImplicit(
                "失敗 %s： %s(%s)",
                signal.getUrl(),
                signal.getCause(),
                signal.getValue()
        );
    }

    @SignalHandler
    public void onDownloadFinished(DownloadSucceedSignal signal)
    {
        this.endDownloads();
    }
}
