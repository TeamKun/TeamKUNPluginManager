package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies;

import lombok.Data;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Data
public class DependencyElement
{
    @NotNull
    private final String pluginName;
    @NotNull
    private final Path pluginPath;
    @NotNull
    private final PluginDescriptionFile pluginDescription;
}
