package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task;

import lombok.Getter;

/**
 * タスクの引数を表します。
 */
public abstract class TaskArgument
{
    @Getter
    private final boolean chain;

    public TaskArgument(TaskResult<?, ?> previousTaskResult)
    {
        this.chain = true;
        if (!previousTaskResult.isSuccess())
            throw new IllegalStateException("Previous must be successful");

    }

    public TaskArgument()
    {
        this.chain = false;
    }
}
