package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.download;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.installer.task.TaskResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * ダウンロードの結果を表すクラスです。
 */
@Getter
public class DownloadResult extends TaskResult<DownloadState, DownloadErrorCause>
{
    /**
     * ダウンロード元のURLです。
     */
    @NotNull
    private final String url;

    /**
     * ダウンロード先のファイルのパスです。
     */
    @NotNull
    private final Path path;
    /**
     * ダウンロードしたファイルのサイズです。
     */
    private final long totalSize;

    /**
     * ダウンロードごとに発行される一意なIDです。
     */
    @NotNull
    private final String downloadID;

    public DownloadResult(boolean success, @NotNull DownloadState taskState, @NotNull String url, @NotNull Path path,
                          long totalSize, @NotNull String downloadID, @Nullable DownloadErrorCause errorCause)
    {
        super(success, taskState, errorCause);
        this.url = url;
        this.path = path;
        this.totalSize = totalSize;
        this.downloadID = downloadID;
    }

    public DownloadResult(boolean success, @NotNull DownloadState taskState, @NotNull String url, @NotNull Path path, long totalSize,
                          @NotNull String downloadID)
    {
        this(success, taskState, url, path, totalSize, downloadID, null);
    }
}
