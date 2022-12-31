package net.kunmc.lab.kpm.task.tasks.dependencies;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.kpm.kpminfo.KPMInformationFile;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

@Value
@AllArgsConstructor
public class DependencyElementImpl implements net.kunmc.lab.kpm.interfaces.task.tasks.dependencies.DependencyElement
{
    @NotNull
    String pluginName;
    @NotNull
    Path pluginPath;
    @NotNull
    PluginDescriptionFile pluginDescription;
    @Nullable
    KPMInformationFile kpmInfoFile;
    @Nullable
    String query;
}
