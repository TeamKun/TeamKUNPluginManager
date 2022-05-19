package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies;

import lombok.Value;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Value
public class DependencyElement
{
    @NotNull
    String pluginName;
    @NotNull
    Path pluginPath;
    @NotNull
    PluginDescriptionFile pluginDescription;
}
