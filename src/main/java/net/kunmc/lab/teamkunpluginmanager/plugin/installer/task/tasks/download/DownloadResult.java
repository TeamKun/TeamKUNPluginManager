package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

@Getter
public class DownloadResult extends TaskResult<DownloadState, DownloadErrorCause>
{
    @Nullable
    private final Path path;
    private final long totalSize;

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
