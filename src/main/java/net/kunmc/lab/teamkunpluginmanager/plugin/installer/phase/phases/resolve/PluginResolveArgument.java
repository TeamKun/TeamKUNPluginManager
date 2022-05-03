package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseArgument;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
public class PluginResolveArgument extends PhaseArgument
{
    @NotNull
    String query;

    public PluginResolveArgument(@NotNull String query)
    {
        this.query = query;
    }
}
