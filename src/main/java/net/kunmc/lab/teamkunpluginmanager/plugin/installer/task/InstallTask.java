package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task;

import lombok.AllArgsConstructor;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import org.jetbrains.annotations.NotNull;

/**
 * インストールに使用するタスクの基底クラスです。
 *
 * @param <A> タスクの引数の型
 * @param <R> タスクの結果の型
 */
@AllArgsConstructor
public abstract class InstallTask<A extends TaskArgument, R extends TaskResult<? extends Enum<?>, ? extends Enum<?>>>
{
    /**
     * インストールごとに生成される、インストール進捗状況です。
     */
    @NotNull
    protected final InstallProgress<?> progress;
    /**
     * タスクからスローされるシグナルを受け取るハンドラーです。
     */
    @NotNull
    private final InstallerSignalHandler signalHandler;

    /**
     * タスクを実行します。
     *
     * @param arguments タスクの引数です。
     * @return タスクの結果です。
     */
    public abstract @NotNull R runTask(@NotNull A arguments);

    protected void postSignal(@NotNull InstallerSignal signal)
    {
        this.signalHandler.handleSignal(this.progress, signal);
    }
}
