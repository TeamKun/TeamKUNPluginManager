package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskArgument;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
public class PluginResolveArgument extends TaskArgument
{
    @NotNull
    String query;

    public PluginResolveArgument(@NotNull String query)
    {
        this.query = query;
    }
}
