package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Value
public class DependencyCollectDependencysDependsFailedSignal implements InstallerSignal
{
    @NotNull
    String pluginName;
    @NotNull
    List<String> collectFailedDependencies;
}
