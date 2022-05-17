package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Data
public class DependencyElement
{
    @NotNull
    private final String pluginName;
    @NotNull
    private final Path pluginPath;

    private int loadOrder;

    public DependencyElement(@NotNull String pluginName, @NotNull Path pluginPath)
    {
        this.pluginName = pluginName;
        this.pluginPath = pluginPath;

        this.loadOrder = -1;
    }
}
