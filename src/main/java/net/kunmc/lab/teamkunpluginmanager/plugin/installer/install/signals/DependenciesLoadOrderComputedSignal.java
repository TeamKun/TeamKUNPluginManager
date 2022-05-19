package net.kunmc.lab.teamkunpluginmanager.plugin.installer.install.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.DependencyElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Data
public class DependenciesLoadOrderComputedSignal implements InstallerSignal
{
    @NotNull
    private final ArrayList<DependencyElement> dependencies;
}
