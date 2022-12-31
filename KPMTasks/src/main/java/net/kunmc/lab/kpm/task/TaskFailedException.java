package net.kunmc.lab.kpm.task;

import lombok.Getter;

/**
 * タスクが失敗したことを表すクラスです。
 */
public class TaskFailedException extends Exception
{
    @Getter
    private final TaskResult<? extends Enum<?>, ? extends Enum<?>> result;

    public TaskFailedException(TaskResult<? extends Enum<?>, ? extends Enum<?>> result)
    {
        this.result = result;
    }

    public TaskFailedException(String message, TaskResult<? extends Enum<?>, ? extends Enum<?>> result)
    {
        super(message);
        this.result = result;
    }

    public TaskFailedException(String message, Throwable cause, TaskResult<? extends Enum<?>, ? extends Enum<?>> result)
    {
        super(message, cause);
        this.result = result;
    }

    public TaskFailedException(Throwable cause, TaskResult<? extends Enum<?>, ? extends Enum<?>> result)
    {
        super(cause);
        this.result = result;
    }
}
