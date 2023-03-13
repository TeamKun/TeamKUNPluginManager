package org.kunlab.kpm.task.tasks.garbage.clean;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.task.AbstractTaskResult;

import java.nio.file.Path;
import java.util.Map;

/**
 * 不要なデータの削除を行うタスクの結果です。
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class GarbageCleanResult extends AbstractTaskResult<GarbageCleanState, GarbageCleanErrorCause>
{
    /**
     * 削除の結果と不要なデータのパスです。
     */
    Map<Path, Boolean> deletedGarbage;

    public GarbageCleanResult(boolean success, @NotNull GarbageCleanState state, @Nullable GarbageCleanErrorCause errorCause, Map<Path, Boolean> deletedGarbage)
    {
        super(success, state, errorCause);
        this.deletedGarbage = deletedGarbage;
    }
}
