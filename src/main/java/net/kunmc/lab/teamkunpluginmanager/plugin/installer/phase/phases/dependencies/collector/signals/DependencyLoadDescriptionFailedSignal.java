package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.collector.signals;

public class DependencyLoadDescriptionFailedSignal extends DependencyCollectFailedSignalBase
{
    public DependencyLoadDescriptionFailedSignal(String failedDependency)
    {
        super(failedDependency);
    }
}
