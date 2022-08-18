package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task;

import lombok.AccessLevel;
import lombok.Setter;
import net.kunmc.lab.teamkunpluginmanager.plugin.AbstractInstaller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

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

    public TaskChain(@NotNull T task, @NotNull IS installerState, @Nullable TaskChain<?, IS, ?, ?, ?> first,
                     @NotNull AbstractInstaller<?, IS> installer)
    {
        this.task = task;
        this.installerState = installerState;
        this.first = first;
        this.installer = installer;
    }

    public TaskChain(@NotNull T task, @NotNull IS installerState, @NotNull AbstractInstaller<?, IS> installer)
    {
        this(task, installerState, null, installer);
        this.first = this;
    }

    public <NTA extends TaskArgument, NTR extends TaskResult<?, ?>> TaskChain<NTA, IS, NTR, R, ?> then(@NotNull IS installerState,
                                                                                                       @NotNull InstallTask<NTA, NTR> nextTask)
    {
        TaskChain<NTA, IS, NTR, R, ?> nextChain = new TaskChain<>(nextTask, installerState, this.first, this.installer);
        this.next = nextChain;
        return nextChain;
    }

    public TaskChain<TA, IS, R, PR, T> bridgeArgument(@NotNull Function<PR, TA> argumentBuilder)
    {
        this.argumentBuilder = argumentBuilder;
        return this;
    }

    public @NotNull TaskResult<?, ?> submitFromThis(@NotNull TaskArgument argument)
    {
        try
        {
            this.installer.getProgress().setCurrentTask(this.installerState);
            R result = this.task.runTask((TA) argument);

            if (result.isSuccess() && this.next != null)
                return this.next.submitFromThis(result);
            else
                return result;
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException("Failed to cast task argument", e);
        }
    }

    public @NotNull TaskResult<?, ?> submitFromThis(@NotNull TaskResult<?, ?> taskResult)
    {
        if (this.argumentBuilder == null)
            throw new IllegalStateException("No argument builder defined to build argument from parent result");

        try
        {
            this.installer.getProgress().setCurrentTask(this.installerState);
            R result = this.task.runTask(this.argumentBuilder.apply((PR) taskResult));

            if (result.isSuccess() && this.next != null)
                return this.next.submitFromThis(result);
            else
                return result;
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException("Failed to cast task result", e);
        }
    }

    public @NotNull TaskResult<?, ?> submitAll(@NotNull TaskArgument argument)
    {
        if (this.first == null)
            throw new IllegalStateException("No task chain defined");
        return this.first.submitFromThis(argument);
    }

}
