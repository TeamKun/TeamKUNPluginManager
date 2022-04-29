package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.FailedReason;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

@Getter
public class DownloadResult extends PhaseResult<DownloadState>
{
    @Nullable
    private final Path path;
    private final long totalSize;

    public DownloadResult(boolean success, @NotNull DownloadState phase, @Nullable Path path,
                          long totalSize, @Nullable FailedReason errorCause)
    {
        super(success, phase, errorCause);
        this.path = path;
        this.totalSize = totalSize;
    }

    public DownloadResult(boolean success, @NotNull DownloadState phase, @Nullable Path path, long totalSize)
    {
        this(success, phase, path, totalSize, null);
    }
}
