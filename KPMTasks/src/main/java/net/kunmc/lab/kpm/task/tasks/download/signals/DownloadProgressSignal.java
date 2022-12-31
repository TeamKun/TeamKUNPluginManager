package net.kunmc.lab.kpm.task.tasks.download.signals;

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
     * ダウンロードしようとしているファイルのURLです。
     */
    @NotNull
    String url;

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

    public DownloadProgressSignal(@NotNull String downloadId, @NotNull String url, long totalSize, long downloaded, double percentage)
    {
        super(downloadId);
        this.url = url;
        this.totalSize = totalSize;
        this.downloaded = downloaded;
        this.percentage = percentage;
    }
}
