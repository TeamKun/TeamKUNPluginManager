package org.kunlab.kpm.task.interfaces;

/**
 * 実行したタスクの結果を表します。
 *
 * @param <S> タスクの状態の型
 * @param <E> タスクのエラー原因の型
 */
public interface TaskResult<S extends Enum<?>, E extends Enum<?>>
{
    /**
     * タスクが成功したかどうかです。
     */
    boolean isSuccess();

    /**
     * タスクの状態です。
     */
    S getState();

    /**
     * タスクのエラー原因です。
     * タスクが成功した場合は {@code null} です。
     */
    E getErrorCause();
}
