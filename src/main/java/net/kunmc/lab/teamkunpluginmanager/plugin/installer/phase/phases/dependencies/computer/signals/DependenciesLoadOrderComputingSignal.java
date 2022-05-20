package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.computer.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.DependencyElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class DependenciesLoadOrderComputingSignal implements InstallerSignal
{
    @NotNull
    private final List<DependencyElement> dependencies;
}
