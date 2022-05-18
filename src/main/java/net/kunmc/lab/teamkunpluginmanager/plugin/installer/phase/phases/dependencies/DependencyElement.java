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

    private int loadOrder;

    public DependencyElement(@NotNull String pluginName, @NotNull Path pluginPath,
                             @NotNull PluginDescriptionFile pluginDescription)
    {
        this.pluginName = pluginName;
        this.pluginPath = pluginPath;
        this.pluginDescription = pluginDescription;

        this.loadOrder = -1;
    }
}
