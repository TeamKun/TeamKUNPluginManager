package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.alias.source.download;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.installer.task.TaskResult;
import net.kunmc.lab.teamkunpluginmanager.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
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
    HashMap<String, Pair<URL, Path>> downloadedSources;

    public SourceDownloadResult(boolean success, @NotNull SourceDownloadState state,
                                @Nullable SourceDownloadErrorCause errorCause, HashMap<String, Pair<URL, Path>> downloadedSources)
    {
        super(success, state, errorCause);
        this.downloadedSources = downloadedSources;
    }

    public SourceDownloadResult(boolean success, @NotNull SourceDownloadState state, @Nullable SourceDownloadErrorCause errorCause)
    {
        this(success, state, errorCause, null);
    }

    public SourceDownloadResult(boolean success, @NotNull SourceDownloadState state, HashMap<String, Pair<URL, Path>> downloadedSources)
    {
        this(success, state, null, downloadedSources);
    }
}
