package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;

import java.util.List;

@AllArgsConstructor
@Value
public class DependsEnumeratedSignal implements InstallerSignal
{
    List<String> dependencies;
    List<String> ignoredDependencies;  // it means already installed or already collected
}
