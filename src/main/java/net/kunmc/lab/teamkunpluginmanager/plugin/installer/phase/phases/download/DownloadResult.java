package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

@Getter
public class DownloadResult extends PhaseResult<DownloadState, DownloadErrorCause>
{
    @Nullable
    private final Path path;
    private final long totalSize;

    @NotNull
    private final String downloadID;

    public DownloadResult(boolean success, @NotNull DownloadState phase, @Nullable Path path,
                          long totalSize, @NotNull String downloadID, @Nullable DownloadErrorCause errorCause)
    {
        super(success, phase, errorCause);
        this.path = path;
        this.totalSize = totalSize;
        this.downloadID = downloadID;
    }

    public DownloadResult(boolean success, @NotNull DownloadState phase, @Nullable Path path, long totalSize,
                          @NotNull String downloadID)
    {
        this(success, phase, path, totalSize, downloadID, null);
    }
}
