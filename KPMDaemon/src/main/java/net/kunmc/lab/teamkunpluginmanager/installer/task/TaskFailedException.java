package net.kunmc.lab.teamkunpluginmanager.installer.task;

import lombok.Getter;

public class TaskFailedException extends Exception
{
    @Getter
    private final TaskResult<?, ?> result;

    public TaskFailedException(TaskResult<?, ?> result)
    {
        this.result = result;
    }

    public TaskFailedException(String message, TaskResult<?, ?> result)
    {
        super(message);
        this.result = result;
    }

    public TaskFailedException(String message, Throwable cause, TaskResult<?, ?> result)
    {
        super(message, cause);
        this.result = result;
    }

    public TaskFailedException(Throwable cause, TaskResult<?, ?> result)
    {
        super(cause);
        this.result = result;
    }
}
