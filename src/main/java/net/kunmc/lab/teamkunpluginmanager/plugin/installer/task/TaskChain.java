package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task;

import lombok.AccessLevel;
import lombok.Setter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.AbstractInstaller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * タスクを連続して実行するために使用される便利なユーティリティです。
 * 内部でインストーラの状態を変更しながら、タスクをステップバイステップで実行していきます。
 * また、前のタスクの結果を次のタスク用の引数に加工するメソッドをサポートします。
 *
 * @param <TA> タスクの引数の型
 * @param <IS> 設定するインストーラの状態
 * @param <R>  タスクの結果の型
 * @param <PR> 親の{@link TaskChain} の結果の方
 * @param <T>  タスクの型
 */
public class TaskChain<
        TA extends TaskArgument,
        IS extends Enum<IS>,
        R extends TaskResult<?, ?>,
        PR extends TaskResult<?, ?>,
        T extends InstallTask<TA, R>>
{
    @NotNull
    private final T task;

    @NotNull
    private final IS installerState;

    @NotNull
    private final AbstractInstaller<?, IS> installer;

    @Nullable
    @Setter(AccessLevel.NONE)
    private TaskChain<?, IS, ?, ?, ?> first;

    @Setter(AccessLevel.PACKAGE)
    private TaskChain<?, IS, ?, ?, ?> next;

    @Nullable
    private Function<PR, TA> argumentBuilder;

    /**
     * {@link TaskChain} を生成します。
     *
     * @param task           タスク
     * @param installerState 設定するインストーラの状態
     * @param first          最初の{@link TaskChain}
     * @param installer      インストーラ
     */
    public TaskChain(@NotNull T task, @NotNull IS installerState, @Nullable TaskChain<?, IS, ?, ?, ?> first,
                     @NotNull AbstractInstaller<?, IS> installer)
    {
        this.task = task;
        this.installerState = installerState;
        this.first = first;
        this.installer = installer;
    }

    /**
     * {@link TaskChain} を生成します。
     *
     * @param task           タスク
     * @param installerState 設定するインストーラの状態
     * @param installer      インストーラ
     */
    public TaskChain(@NotNull T task, @NotNull IS installerState, @NotNull AbstractInstaller<?, IS> installer)
    {
        this(task, installerState, null, installer);
        this.first = this;
    }

    /**
     * タスクをつなげます。
     *
     * @param installerState 設定するインストーラの状態
     * @param nextTask       次のタスク
     * @param <NTA>          次のタスクの引数の型
     * @param <NTR>          次のタスクの結果の型
     * @return 次の{@link TaskChain}
     */
    public <NTA extends TaskArgument, NTR extends TaskResult<?, ?>> TaskChain<NTA, IS, NTR, R, ?> then(@NotNull IS installerState,
                                                                                                       @NotNull InstallTask<NTA, NTR> nextTask)
    {
        TaskChain<NTA, IS, NTR, R, ?> nextChain = new TaskChain<>(nextTask, installerState, this.first, this.installer);
        this.next = nextChain;
        return nextChain;
    }

    /**
     * 前のタスクの {@link TaskResult} を引数にして、次のタスクの引数を生成する関数を設定します。
     *
     * @param argumentBuilder 前のタスクの {@link TaskResult} を引数にして、次のタスクの引数を生成する関数
     * @return この{@link TaskChain}
     */
    public TaskChain<TA, IS, R, PR, T> bridgeArgument(@NotNull Function<PR, TA> argumentBuilder)
    {
        this.argumentBuilder = argumentBuilder;
        return this;
    }

    /**
     * このタスクから登録されたタスクを実行していきます。
     *
     * @param argument タスクの引数
     * @return タスクの結果
     * @throws TaskFailedException タスクの実行に失敗した場合
     */
    public @NotNull TaskResult<?, ?> submitFromThis(@NotNull TaskArgument argument) throws TaskFailedException
    {
        try
        {
            this.installer.getProgress().setCurrentTask(this.installerState);
            R result = this.task.runTask((TA) argument);

            if (!result.isSuccess())
                throw new TaskFailedException(result);

            if (this.next != null)
                return this.next.submitFromThis(result);
            else
                return result;
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException("Failed to cast task argument", e);
        }
    }

    /**
     * このタスクから登録されたタスクを実行していきます。
     *
     * @param taskResult 前のタスクの結果
     * @return タスクの結果
     * @throws TaskFailedException タスクの実行に失敗した場合
     */
    public @NotNull TaskResult<?, ?> submitFromThis(@NotNull TaskResult<?, ?> taskResult) throws TaskFailedException
    {
        if (this.argumentBuilder == null)
            throw new IllegalStateException("No argument builder defined to build argument from parent result");

        try
        {
            this.installer.getProgress().setCurrentTask(this.installerState);
            R result = this.task.runTask(this.argumentBuilder.apply((PR) taskResult));

            if (!result.isSuccess())
                throw new TaskFailedException(result);

            if (this.next != null)
                return this.next.submitFromThis(result);
            else
                return result;
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException("Failed to cast task result", e);
        }
    }

    /**
     * 最初のタスクから登録されたタスクを実行していきます。
     *
     * @param argument タスクの引数
     * @return タスクの結果
     * @throws TaskFailedException   タスクの実行に失敗した場合
     * @throws IllegalStateException タスクチェーンが設定されていない場合
     */
    public @NotNull TaskResult<?, ?> submitAll(@NotNull TaskArgument argument) throws TaskFailedException, IllegalStateException
    {
        if (this.first == null)
            throw new IllegalStateException("No task chain defined");
        return this.first.submitFromThis(argument);
    }

}
