package net.kunmc.lab.kpm.installer.task.tasks.garbage.clean;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.installer.task.TaskResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;

/**
 * 不要データ削除を行うタスクの結果です。
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class GarbageCleanResult extends TaskResult<GarbageCleanState, GarbageCleanErrorCause>
{
    /**
     * 削除の結果と不要データのパスです。
     */
    HashMap<Path, Boolean> deletedGarbage;

    public GarbageCleanResult(boolean success, @NotNull GarbageCleanState state, @Nullable GarbageCleanErrorCause errorCause, HashMap<Path, Boolean> deletedGarbage)
    {
        super(success, state, errorCause);
        this.deletedGarbage = deletedGarbage;
    }
}
