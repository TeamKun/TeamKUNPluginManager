package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals;

public class DependsLoadDescriptionFailedSignal extends DependencyCollectFailedSignalBase
{
    public DependsLoadDescriptionFailedSignal(String failedDependency)
    {
        super(failedDependency);
    }
}
