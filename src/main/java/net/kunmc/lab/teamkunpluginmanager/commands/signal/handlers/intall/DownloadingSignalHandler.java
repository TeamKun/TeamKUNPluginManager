package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.intall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Progressbar;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals.DependsDownloadFinishedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.description.signals.LoadPluginDescriptionSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.signals.DownloadErrorSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.signals.DownloadProgressSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.ChatColor;

/**
 * ダウンロードのシグナルを処理するハンドラです.
 */
public class DownloadingSignalHandler
{
    private final Terminal terminal;
    private String currentDownload;

    // Ignore DownloadStartingSignal
    private long downloads;
    private long downloadTotalSize;
    private long downloadStarted;
    private Progressbar downloadProgressBar;
    private boolean mainDescriptionLoaded;

    public DownloadingSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    private void startDownloads(String id)
    {
        this.currentDownload = id;
        this.downloads = 0;
        this.downloadTotalSize = 0;
        this.downloadStarted = System.currentTimeMillis();
        this.downloadProgressBar = terminal.createProgressbar("ダウンロード");
        this.downloadProgressBar.setProgressMax(100);
    }

    private void addDownloadArtifact(String url, long size)
    {
        this.downloadTotalSize += size;
        terminal.writeLine(ChatColor.GREEN + "取得:" + ++downloads + " " + url + " [" + PluginUtil.getFileSizeString(size) + "]");
    }

    private void endDownloads()
    {
        long elapsedMillis = System.currentTimeMillis() - this.downloadStarted;
        long elapsedSec = elapsedMillis / 1000;
        long bytesPerSec = this.downloadTotalSize / elapsedSec;
        terminal.writeLine(
                ChatColor.GREEN + PluginUtil.getFileSizeString(this.downloadTotalSize) + " を " +
                        ChatColor.YELLOW + elapsedSec + "秒" +
                        ChatColor.GREEN + "で取得しました (" +
                        ChatColor.YELLOW + PluginUtil.getFileSizeString(bytesPerSec) + "/s)"
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

        if (!currentDownload.equals(signal.getDownloadId()))
            this.addDownloadArtifact(signal.getUrl(), signal.getTotalSize());

        double percent = (double) signal.getDownloaded() / signal.getTotalSize();
        this.downloadProgressBar.setProgress((int) (percent * 100));  // max 100
    }

    @SignalHandler
    public void onDownloadsEnd(LoadPluginDescriptionSignal signal)
    {
        // Typically, this signal is called after all downloads are finished.
        // But, first time, this signal is called after one download to resolve dependencies.
        // So, we need to handle first time only.

        if (mainDescriptionLoaded)
            return;

        mainDescriptionLoaded = true;
        this.endDownloads();
    }

    @SignalHandler
    public void onDownloadsEnd(DependsDownloadFinishedSignal signal)
    {
        this.endDownloads();
    }

    @SignalHandler
    public void onDownloadFailed(DownloadErrorSignal signal)
    {
        terminal.writeLine(String.format(ChatColor.RED + "失敗:%d %s: %s(%s)",
                this.downloads, signal.getUrl(), signal.getCause(), signal.getValue()
        ));
    }
}
