package org.kunlab.kpm.task.tasks.download.signals;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * ダウンロードの開始中であることを示すシグナルです。
 */
@Getter
@Setter
public class DownloadStartingSignal extends DownloadSignal
{
    /**
     * ダウンロード先のパスです。
     */
    @NotNull
    private Path downloadPath;
    /**
     * ダウンロードを行うURLです。
     */
    @NotNull
    private String url;

    public DownloadStartingSignal(@NotNull String downloadId, @NotNull Path downloadPath, @NotNull String url)
    {
        super(downloadId);
        this.downloadPath = downloadPath;
        this.url = url;
    }
}
