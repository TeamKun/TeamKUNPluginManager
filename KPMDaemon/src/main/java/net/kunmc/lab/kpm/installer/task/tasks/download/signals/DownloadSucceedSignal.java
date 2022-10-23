package net.kunmc.lab.kpm.installer.task.tasks.download.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * ダウンロードが完了したことを示すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class DownloadSucceedSignal extends DownloadSignal
{
    /**
     * ダウンロードしたファイルのパスです。
     */
    @NotNull
    Path downloadPath;
    /**
     * ダウンロードしたファイルのサイズです。
     */
    long totalSize;

    public DownloadSucceedSignal(@NotNull String downloadId, @NotNull Path downloadPath, long totalSize)
    {
        super(downloadId);
        this.downloadPath = downloadPath;
        this.totalSize = totalSize;
    }
}
