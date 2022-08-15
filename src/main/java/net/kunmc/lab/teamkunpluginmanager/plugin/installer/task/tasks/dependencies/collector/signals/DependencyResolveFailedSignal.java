package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals;

public class DependencyResolveFailedSignal extends DependencyCollectFailedSignalBase
{
    public DependencyResolveFailedSignal(String failedDependency)
    {
        super(failedDependency);
    }
}
