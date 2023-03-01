package org.kunlab.kpm.task;

import lombok.Getter;
import org.kunlab.kpm.task.interfaces.TaskResult;

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
