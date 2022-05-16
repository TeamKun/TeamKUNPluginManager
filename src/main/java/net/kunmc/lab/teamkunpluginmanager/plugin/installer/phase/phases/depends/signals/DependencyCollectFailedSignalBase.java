package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;

@AllArgsConstructor
@Getter
public abstract class DependencyCollectFailedSignalBase implements InstallerSignal
{
    private final String failedDependency;
}
