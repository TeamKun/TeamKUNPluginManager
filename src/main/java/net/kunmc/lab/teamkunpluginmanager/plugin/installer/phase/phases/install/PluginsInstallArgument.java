package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.install;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.DependencyElement;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = false)
public class PluginsInstallArgument extends PhaseArgument
{
    @NotNull
    Path pluginPath;
    @NotNull
    PluginDescriptionFile pluginDescription;

    @NotNull
    List<DependencyElement> dependencies;
}
