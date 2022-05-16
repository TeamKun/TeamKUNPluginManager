package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals;

public class DependsResolveFailedSignal extends DependencyCollectFailedSignalBase
{
    public DependsResolveFailedSignal(String failedDependency)
    {
        super(failedDependency);
    }
}
