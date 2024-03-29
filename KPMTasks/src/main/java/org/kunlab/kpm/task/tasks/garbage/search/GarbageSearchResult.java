package org.kunlab.kpm.task.tasks.garbage.search;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.task.AbstractTaskResult;

import java.nio.file.Path;
import java.util.List;

/**
 * 不要なデータの検索を行うタスクの結果です。
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class GarbageSearchResult extends AbstractTaskResult<GarbageSearchState, GarbageSearchErrorCause>
{
    /**
     * 見つかった不要なデータのパスのリストです。
     */
    List<Path> garbageFiles;

    public GarbageSearchResult(boolean success, @NotNull GarbageSearchState state, @Nullable GarbageSearchErrorCause errorCause, List<Path> garbageFiles)
    {
        super(success, state, errorCause);
        this.garbageFiles = garbageFiles;
    }

    public GarbageSearchResult(@NotNull GarbageSearchState state, List<Path> garbageFiles)
    {
        this(true, state, null, garbageFiles);
    }

    public GarbageSearchResult(boolean success, @NotNull GarbageSearchState state, @Nullable GarbageSearchErrorCause errorCause)
    {
        this(success, state, errorCause, null);
    }
}
