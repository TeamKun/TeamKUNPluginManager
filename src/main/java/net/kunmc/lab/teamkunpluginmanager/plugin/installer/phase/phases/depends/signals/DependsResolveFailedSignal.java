package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;

@Value
public class DependsResolveFailedSignal implements InstallerSignal
{
    String resolveFailedDependencies;
}
