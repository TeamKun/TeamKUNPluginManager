package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskArgument;
import org.jetbrains.annotations.NotNull;

/**
 * プラグインの解決を行うタスクの引数です。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PluginResolveArgument extends TaskArgument
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
