package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task;

import net.kunmc.lab.teamkunpluginmanager.plugin.AbstractInstaller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

public class TaskSubmitter<
        T extends TaskArgument,
        TS extends Enum<TS>,
        I extends AbstractInstaller<?, TS>,
        A extends TaskArgument,
        R extends TaskResult<?, ?>,
        E extends InstallTask<A, R>>
{
    @NotNull
    private final I installer;
    @NotNull
    private final E task;
    @NotNull
    private final TS taskState;

    @NotNull
    private final TaskSubmitter<T, TS, I, ?, ?, ?> first;
    @Nullable
    private TaskSubmitter<T, TS, I, ?, ?, ?> next;

    public TaskSubmitter(@NotNull TS taskState, @NotNull I installer, @NotNull E task)
    {
        this(taskState, installer, task, null, null);
    }

    private TaskSubmitter(@NotNull TS taskState, @NotNull I installer, @NotNull E task,
                          @Nullable TaskSubmitter<T, TS, I, ?, ?, ?> first, @Nullable TaskSubmitter<T, TS, I, ?, ?, ?> next)
    {
        this.installer = installer;
        this.task = task;
        this.taskState = taskState;

        this.next = next;

        if (first == null)
            this.first = this;
        else
            this.first = first;
    }

    @SuppressWarnings("unchecked")
    private static <T extends InstallTask<?, ?>> Class<? extends TaskArgument> getTaskArgumentOf(@NotNull T task)
    {
        return (Class<? extends TaskArgument>) ((ParameterizedType) task.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    private static Constructor<TaskArgument> getConstructor(@NotNull Class<? extends TaskArgument> clazz,
                                                            @NotNull TaskResult<?, ?> parentResult)
    {

        try
        {
            for (Constructor<?> constructor : clazz.getConstructors())
            {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(parentResult.getClass()))
                {
                    constructor.setAccessible(true);
                    return (Constructor<TaskArgument>) constructor;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        throw new IllegalArgumentException("No constructor found for " + clazz.getName());
    }

    public <TA extends TaskArgument, TR extends TaskResult<?, ?>, TE extends InstallTask<TA, TR>>
    @NotNull TaskSubmitter<T, TS, I, TA, TR, TE> then(@NotNull TS taskState, @NotNull TE task)
    {
        TaskSubmitter<T, TS, I, TA, TR, TE> submitter = new TaskSubmitter<>(taskState, installer, task, this.first, null);
        this.next = submitter;

        return submitter;
    }

    public @NotNull R submitOnce(@NotNull A argument)
    {
        return this.task.runTask(argument);
    }

    @SuppressWarnings("unchecked")
    protected <PA extends TaskArgument> @NotNull R submitOnceUnsafe(@NotNull PA argument)
    {
        return this.task.runTask((A) argument);
    }

    public @NotNull TaskResult<?, ?> submit(@NotNull T firstArgument)
    {
        TaskSubmitter<T, TS, I, ?, ?, ?> submitter = this.first;

        installer.getProgress().setCurrentTask(taskState);
        TaskResult<?, ?> result = submitter.submitOnceUnsafe(firstArgument);

        try
        {
            while (submitter.next != null)
            {
                if (!result.isSuccess())
                    return result;

                submitter = submitter.next;

                installer.getProgress().setCurrentTask(submitter.taskState);
                TaskArgument nextArgument = getConstructor(getTaskArgumentOf(submitter.task), result).newInstance(result);

                result = submitter.submitOnceUnsafe(nextArgument);

            }
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
