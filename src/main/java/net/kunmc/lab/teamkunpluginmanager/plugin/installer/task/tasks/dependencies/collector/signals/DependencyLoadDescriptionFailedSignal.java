package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals;

public class DependencyLoadDescriptionFailedSignal extends DependencyCollectFailedSignalBase
{
    public DependencyLoadDescriptionFailedSignal(String failedDependency)
    {
        super(failedDependency);
    }
}
