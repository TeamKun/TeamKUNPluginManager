package net.kunmc.lab.kpm.task;

/**
 * 実行したタスクの結果を表します。
 *
 * @param <S>  タスクの状態の型
 * @param <EC> タスクのエラー原因の型
 */
public interface TaskResult<S extends Enum<?>, EC extends Enum<?>>
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
    EC getErrorCause();
}
