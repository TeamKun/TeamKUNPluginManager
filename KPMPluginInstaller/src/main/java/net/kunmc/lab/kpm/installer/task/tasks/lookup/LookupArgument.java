package net.kunmc.lab.kpm.installer.task.tasks.lookup;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.installer.task.TaskArgument;
import org.jetbrains.annotations.NotNull;

/**
 * プラグインの検索を行うタスクの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class LookupArgument extends TaskArgument
{
    /**
     * 検索するクエリです。
     */
    @NotNull
    String[] queries;
}
