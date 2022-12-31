package net.kunmc.lab.kpm.task;

import lombok.Getter;
import net.kunmc.lab.kpm.interfaces.task.TaskResult;

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
}
