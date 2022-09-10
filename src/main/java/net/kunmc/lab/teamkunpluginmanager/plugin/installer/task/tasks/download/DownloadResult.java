package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskResult;
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
     * ダウンロード先のファイルのパスです。
     */
    @Nullable
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

    public DownloadResult(boolean success, @NotNull DownloadState taskState, @Nullable Path path,
                          long totalSize, @NotNull String downloadID, @Nullable DownloadErrorCause errorCause)
    {
        super(success, taskState, errorCause);
        this.path = path;
        this.totalSize = totalSize;
        this.downloadID = downloadID;
    }

    public DownloadResult(boolean success, @NotNull DownloadState taskState, @Nullable Path path, long totalSize,
                          @NotNull String downloadID)
    {
        this(success, taskState, path, totalSize, downloadID, null);
    }
}