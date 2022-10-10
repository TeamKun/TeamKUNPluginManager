package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.common;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Progressbar;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.download.signals.DownloadErrorSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.download.signals.DownloadProgressSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.download.signals.DownloadSucceedSignal;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.utils.Utils;
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
        this.downloadProgressBar = terminal.createProgressbar("ダウンロード");
        this.downloadProgressBar.setProgressMax(100);
    }

    private void addDownloadArtifact(String url, long size)
    {
        this.downloadTotalSize += size;
        terminal.writeLine(ChatColor.GREEN + "取得 " + url + " [" + Utils.roundSizeUnit(size) + "]");
    }

    private void endDownloads()
    {
        long elapsedMillis = System.currentTimeMillis() - this.downloadStarted;
        long elapsedSec = elapsedMillis / 1000;
        if (elapsedSec == 0)
            elapsedSec = 1;
        long bytesPerSec = this.downloadTotalSize / elapsedSec;
        terminal.writeLine(
                ChatColor.GREEN + Utils.roundSizeUnit(this.downloadTotalSize) + " を " +
                        ChatColor.YELLOW + elapsedSec + "秒" +
                        ChatColor.GREEN + "で取得しました (" +
                        ChatColor.YELLOW + Utils.roundSizeUnit(bytesPerSec) + "/s" +
                        ChatColor.GREEN + ")"
        );

        this.currentDownload = null;
    }

    @SignalHandler
    public void onDownloadingSignal(DownloadProgressSignal signal)
    {
        if (currentDownload == null)
        {
            this.startDownloads(signal.getDownloadId());
            this.addDownloadArtifact(signal.getUrl(), signal.getTotalSize());
        }

        double percent = (double) signal.getDownloaded() / signal.getTotalSize();
        this.downloadProgressBar.setProgress((int) (percent * 100));  // max 100
    }

    @SignalHandler
    public void onDownloadFailed(DownloadErrorSignal signal)
    {
        terminal.writeLine(String.format(ChatColor.RED + "失敗 %s: %s(%s)",
                signal.getUrl(), signal.getCause(), signal.getValue()
        ));
    }

    @SignalHandler
    public void onDownloadFinished(DownloadSucceedSignal signal)
    {
        this.endDownloads();
    }
}
