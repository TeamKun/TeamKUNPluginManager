package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals;

public class DependencyLoadDescriptionFailedSignal extends DependencyCollectFailedSignalBase
{
    public DependencyLoadDescriptionFailedSignal(String failedDependency)
    {
        super(failedDependency);
    }
}
