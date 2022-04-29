package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseArgument;
import org.jetbrains.annotations.NotNull;

@Data
public class PluginResolveArgument implements PhaseArgument
{
    @NotNull
    String query;
}
