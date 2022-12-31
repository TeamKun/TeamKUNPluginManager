package net.kunmc.lab.kpm.task;

/**
 * インストールに使用するタスクの基底クラスです。
 *
 * @param <A> タスクの引数の型
 * @param <R> タスクの結果の型
 */
public interface InstallTask<A extends TaskArgument, R extends TaskResult<? extends Enum<?>, ? extends Enum<?>>>
{
    /**
     * タスクを実行します。
     *
     * @param arguments タスクの引数です。
     * @return タスクの結果です。
     */
    R runTask(A arguments);
}
