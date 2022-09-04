package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * ダウンロードの進捗を表すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class DownloadProgressSignal extends DownloadSignal
{
    /**
     * ダウンロードしようとしているファイルのサイズです。
     */
    long totalSize;
    /**
     * ダウンロードしたファイルのサイズです。
     */
    long downloaded;

    /**
     * ダウンロードの進捗を表す値で、 {@code 0.0} から {@code 1.0} の間の値です。
     * {@code 0.5}は、ダウンロードが半分完了したことを表します。
     */
    double percentage;

    public DownloadProgressSignal(@NotNull String downloadId, long totalSize, long downloaded, double percentage)
    {
        super(downloadId);
        this.totalSize = totalSize;
        this.downloaded = downloaded;
        this.percentage = percentage;
    }
}
