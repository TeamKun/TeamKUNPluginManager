package org.kunlab.kpm.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.task.interfaces.TaskResult;

/**
 * 実行したタスクの結果を表します。
 *
 * @param <S>  タスクの状態の型
 * @param <EC> タスクのエラー原因の型
 */
@AllArgsConstructor
@Getter
public abstract class AbstractTaskResult<S extends Enum<?>, EC extends Enum<?>> implements TaskResult<S, EC>
{
    /**
     * タスクが成功したかどうかです。
     */
    private final boolean success;
    /**
     * タスクの状態です。
     */
    @NotNull
    private final S state;

    /**
     * タスクのエラー原因です。
     * タスクが成功した場合は {@code null} です。
     */
    @Nullable
    private final EC errorCause;
}
