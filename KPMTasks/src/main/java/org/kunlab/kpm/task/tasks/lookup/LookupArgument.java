package org.kunlab.kpm.task.tasks.lookup;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.interfaces.task.TaskArgument;

/**
 * プラグインの検索を行うタスクの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class LookupArgument implements TaskArgument
{
    /**
     * 検索するクエリです。
     */
    @NotNull
    String[] queries;
}
