package net.kunmc.lab.kpm.installer.task.tasks.alias.source.download;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.installer.task.TaskResult;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * ソースファイルのダウンロードを行うタスクの結果です。
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class SourceDownloadResult extends TaskResult<SourceDownloadState, SourceDownloadErrorCause>
{
    /**
     * ダウンロードしたソースの名前とパスのマップです。
     */
    HashMap<String, Pair<URI, Path>> downloadedSources;

    public SourceDownloadResult(boolean success, @NotNull SourceDownloadState state,
                                @Nullable SourceDownloadErrorCause errorCause, HashMap<String, Pair<URI, Path>> downloadedSources)
    {
        super(success, state, errorCause);
        this.downloadedSources = downloadedSources;
    }

    public SourceDownloadResult(boolean success, @NotNull SourceDownloadState state, @Nullable SourceDownloadErrorCause errorCause)
    {
        this(success, state, errorCause, null);
    }

    public SourceDownloadResult(boolean success, @NotNull SourceDownloadState state, HashMap<String, Pair<URI, Path>> downloadedSources)
    {
        this(success, state, null, downloadedSources);
    }
}
