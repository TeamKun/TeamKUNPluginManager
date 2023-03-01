package org.kunlab.kpm.installer;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.installer.interfaces.InstallProgress;
import org.kunlab.kpm.installer.interfaces.InstallResult;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;
import org.kunlab.kpm.installer.interfaces.PluginInstaller;
import org.kunlab.kpm.installer.signals.InstallFinishedSignal;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.signal.Signal;
import org.kunlab.kpm.signal.SignalHandleManager;
import org.kunlab.kpm.task.TaskChain;
import org.kunlab.kpm.task.TaskFailedException;
import org.kunlab.kpm.task.interfaces.InstallTask;
import org.kunlab.kpm.task.interfaces.TaskArgument;
import org.kunlab.kpm.task.interfaces.TaskResult;

import java.io.File;
import java.io.IOException;

/**
 * インストーラの基底クラスです。
 *
 * @param <A> インストーラの引数の型
 * @param <E> インストールのタスクの列挙型
 * @param <P> インストールのタスクの引数の型
 */
public abstract class AbstractInstaller<A extends InstallerArgument, E extends Enum<E>, P extends Enum<P>> implements PluginInstaller<A, E, P>
{
    @Getter
    protected final KPMRegistry registry;
    @Getter
    protected final InstallProgress<P, PluginInstaller<A, E, P>> progress;
    protected final SignalHandleManager signalHandler;

    public AbstractInstaller(@NotNull KPMRegistry registry, @NotNull SignalHandleManager signalHandler) throws IOException
    {
        this.registry = registry;
        this.signalHandler = signalHandler;

        this.progress = InstallProgressImpl.of(this, signalHandler, null);
    }

    /**
     * シグナルを送信します。
     *
     * @param signal シグナル
     */
    protected void postSignal(@NotNull Signal signal)
    {
        this.signalHandler.handleSignal(signal);
    }

    /**
     * インストーラを実行します。
     * このメソッドを直接呼び出すことは推奨されておらず、{@link AbstractInstaller#run(InstallerArgument)}を使用してください。
     *
     * @param arguments インストーラに渡す引数
     * @return インストールの結果
     * @throws IOException         ファイルの読み書きに失敗した場合
     * @throws TaskFailedException インストールの途中でタスクが失敗した場合
     * @see AbstractInstaller#run(InstallerArgument)
     */
    protected abstract InstallResult<P> execute(@NotNull A arguments) throws IOException, TaskFailedException;

    /**
     * インストーラを実行します。
     * このメソッドは、内部で{@link AbstractInstaller#execute(InstallerArgument)}を呼び出します。
     *
     * @param arguments インストーラに渡す引数
     * @return インストールの結果
     * @see AbstractInstaller#execute(InstallerArgument)
     */
    @Override
    public InstallResult<P> run(@NotNull A arguments)
    {
        try
        {
            return this.execute(arguments);
        }
        catch (TaskFailedException e)
        {
            return this.handleTaskError(e.getResult());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            InstallFailedInstallResult<P, ?, ?> result = new InstallFailedInstallResult<>(this.progress, e);
            this.postSignal(new InstallFinishedSignal(result));

            return result;
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
        InstallResult<P> result = new InstallResultImpl<>(true, this.progress);
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
    {
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
    {
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
     * @param <AA>      タスクの引数の型
     * @param <R>       タスクの戻り値の型
     * @param <TT>      タスクの列挙型
     * @return タスクのSubmitter
     */
    @NotNull
    protected <AA extends TaskArgument, R extends TaskResult<?, ?>, TT extends InstallTask<AA, R>>
    TaskChain<AA, P, R, R, TT>
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
        return this.getRegistry().getEnvironment().getExcludes().stream()
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
