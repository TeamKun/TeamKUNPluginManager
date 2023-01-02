package net.kunmc.lab.kpm.task.tasks.garbage.clean;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.task.AbstractTaskResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;

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
    HashMap<Path, Boolean> deletedGarbage;

    public GarbageCleanResult(boolean success, @NotNull GarbageCleanState state, @Nullable GarbageCleanErrorCause errorCause, HashMap<Path, Boolean> deletedGarbage)
    {
        super(success, state, errorCause);
        this.deletedGarbage = deletedGarbage;
    }
}
