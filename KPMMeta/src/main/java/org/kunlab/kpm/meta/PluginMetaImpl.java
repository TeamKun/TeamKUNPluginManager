package org.kunlab.kpm.meta;

import lombok.Value;
import org.bukkit.plugin.PluginLoadOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.meta.interfaces.DependencyNode;
import org.kunlab.kpm.meta.interfaces.PluginMeta;

import java.util.List;

@Value
class PluginMetaImpl implements PluginMeta
{
    @NotNull
    String name;
    @NotNull
    String version;
    @NotNull
    PluginLoadOrder loadTiming;

    @NotNull
    InstallOperator installedBy;
    boolean isDependency;
    @Nullable
    String resolveQuery;

    long installedAt;
    @NotNull
    List<String> authors;

    @NotNull
    List<DependencyNode> dependedBy;
    @NotNull
    List<DependencyNode> dependsOn;
}
