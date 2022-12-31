package net.kunmc.lab.kpm.task.tasks.lookup;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.task.TaskArgument;
import org.jetbrains.annotations.NotNull;

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
