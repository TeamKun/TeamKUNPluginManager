package net.kunmc.lab.kpm.task.tasks.resolve;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.interfaces.task.TaskArgument;
import org.jetbrains.annotations.NotNull;

/**
 * プラグインの解決を行うタスクの引数です。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PluginResolveArgument implements TaskArgument
{
    /**
     * 解決する際に使用されるクエリです。
     */
    @NotNull
    String query;

    public PluginResolveArgument(@NotNull String query)
    {
        this.query = query;
    }
}
