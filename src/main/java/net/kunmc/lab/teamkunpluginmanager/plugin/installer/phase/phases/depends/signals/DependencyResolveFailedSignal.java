package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals;

public class DependencyResolveFailedSignal extends DependencyCollectFailedSignalBase
{
    public DependencyResolveFailedSignal(String failedDependency)
    {
        super(failedDependency);
    }
}
