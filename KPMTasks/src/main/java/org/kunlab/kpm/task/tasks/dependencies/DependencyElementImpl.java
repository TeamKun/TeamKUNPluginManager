package org.kunlab.kpm.task.tasks.dependencies;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.kpminfo.KPMInformationFile;
import org.kunlab.kpm.task.interfaces.dependencies.DependencyElement;

import java.nio.file.Path;

@Value
@AllArgsConstructor
public class DependencyElementImpl implements DependencyElement
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
