package net.kunmc.lab.teamkunpluginmanager.installer;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.KPMDaemon;
import net.kunmc.lab.teamkunpluginmanager.installer.signals.InstallFinishedSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.task.InstallTask;
import net.kunmc.lab.teamkunpluginmanager.installer.task.TaskArgument;
import net.kunmc.lab.teamkunpluginmanager.installer.task.TaskChain;
import net.kunmc.lab.teamkunpluginmanager.installer.task.TaskFailedException;
import net.kunmc.lab.teamkunpluginmanager.installer.task.TaskResult;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandleManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * インストーラの基底クラスです。
 *
 * @param <A> インストーラの引数の型
 * @param <E> インストールのタスクの列挙型
 * @param <P> インストールのタスクの引数の型
 */
public abstract class AbstractInstaller<A extends AbstractInstallerArgument, E extends Enum<E>, P extends Enum<P>>
{
    protected final KPMDaemon daemon;
    @Getter
    protected final InstallProgress<P, AbstractInstaller<A, E, P>> progress;
    protected final SignalHandleManager signalHandler;

    public AbstractInstaller(@NotNull KPMDaemon daemon, @NotNull SignalHandleManager signalHandler) throws IOException
    {
        this.daemon = daemon;
        this.signalHandler = signalHandler;

        this.progress = InstallProgress.of(this, signalHandler, null);
    }

    /**
     * シグナルを送信します。
     *
     * @param signal シグナル
     */
    protected void postSignal(@NotNull Signal signal)
    {
        this.signalHandler.handleSignal(this.progress, signal);
    }

    /**
     * インストーラを実行します。
     * このメソッドを直接呼び出すことは推奨されておらず、{@link AbstractInstaller#run(AbstractInstallerArgument)}を使用してください。
     *
     * @param arguments インストーラに渡す引数
     * @return インストールの結果
     * @throws IOException         ファイルの読み書きに失敗した場合
     * @throws TaskFailedException インストールの途中でタスクが失敗した場合
     * @see AbstractInstaller#run(AbstractInstallerArgument)
     */
    protected abstract InstallResult<P> execute(@NotNull A arguments) throws IOException, TaskFailedException;

    /**
     * インストーラを実行します。
     * このメソッドは、内部で{@link AbstractInstaller#execute(AbstractInstallerArgument)}を呼び出します。
     *
     * @param arguments インストーラに渡す引数
     * @return インストールの結果
     * @throws IOException ファイルの読み書きに失敗した場合
     * @see AbstractInstaller#execute(AbstractInstallerArgument)
     */
    public InstallResult<P> run(@NotNull A arguments) throws IOException
    {
        try
        {
            return this.execute(arguments);
        }
        catch (TaskFailedException e)
        {
            return this.handleTaskError(e.getResult());
        }
    }

    /**
     * インストールが成功したときの後始末を行います。
     *
     * @return インストールの結果
     */
    @NotNull
    protected InstallResult<P> success()
    {
        InstallResult<P> result = new InstallResult<>(true, this.progress);
        this.postSignal(new InstallFinishedSignal(result));

        return result;
    }

    /**
     * インストールが失敗したときの後始末を引数を指定して行います。
     *
     * @param customResult インストールの結果
     * @return インストールの結果
     */
    @NotNull
    protected InstallResult<P> success(InstallResult<P> customResult)
    {
        if (!customResult.isSuccess())
            throw new IllegalArgumentException("customResult must be success.");

        this.postSignal(new InstallFinishedSignal(customResult));

        return customResult;
    }

    /**
     * インストールが失敗したときの後始末を行います。
     *
     * @param reason 失敗の理由
     * @param <T>    インストールのタスクの列挙型
     * @return インストールの結果
     */
    public <T extends Enum<T>> InstallFailedInstallResult<P, T, ?> error(@NotNull T reason)
    {  // TODO: Implement debug mode
        InstallFailedInstallResult<P, T, ?> result = new InstallFailedInstallResult<>(this.progress, reason);
        this.postSignal(new InstallFinishedSignal(result));

        return result;
    }

    /**
     * インストールが失敗したときの後始末を行います。
     *
     * @param reason     失敗の理由
     * @param taskStatus タスクの状態
     * @param <T>        インストールのタスクの列挙型
     * @param <S>        インストールのタスクの引数の型
     * @return インストールの結果
     */
    public <T extends Enum<T>, S extends Enum<S>> InstallFailedInstallResult<P, T, S> error(
            @Nullable T reason,
            @NotNull S taskStatus)
    {  // TODO: Implement debug mode
        InstallFailedInstallResult<P, T, S> result = new InstallFailedInstallResult<>(this.progress, reason, taskStatus);
        this.postSignal(new InstallFinishedSignal(result));

        return result;
    }

    @NotNull
    @SuppressWarnings({"rawtypes", "unchecked"})
    private InstallResult<P> handleTaskError(@NotNull TaskResult result)
    {
        if (result.getErrorCause() != null)
            return this.error(result.getErrorCause(), result.getState());
        else
            return this.error(null, result.getState());
    }

    /**
     * タスクのSubmitterを取得します。
     *
     * @param taskState タスクの状態
     * @param task      タスク
     * @param <A>       タスクの引数の型
     * @param <R>       タスクの戻り値の型
     * @param <TT>      タスクの列挙型
     * @return タスクのSubmitter
     */
    @NotNull
    protected <A extends TaskArgument, R extends TaskResult<?, ?>, TT extends InstallTask<A, R>>
    TaskChain<A, P, R, R, TT>
    submitter(@NotNull P taskState, @NotNull TT task)
    {
        return new TaskChain<>(task, taskState, this);
    }

    /**
     * プラグインが無視リストに入っているかどうかを取得します。
     *
     * @param pluginName プラグインの名前
     * @return プラグインが無視リストに入っているかどうか
     */
    protected boolean isPluginIgnored(@NotNull String pluginName)
    {
        return KPMDaemon.getInstance().getEnvs().getExcludes().stream()
                .parallel()
                .anyMatch(s -> s.equalsIgnoreCase(pluginName));
    }

    /**
     * ファイルを安全に削除します。
     *
     * @param f 削除するファイル
     * @return 削除に成功したかどうか
     */
    protected boolean safeDelete(@NotNull File f)
    {
        try
        {
            return f.delete();
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
