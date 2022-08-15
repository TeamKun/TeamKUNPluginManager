package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals;

public class DependencyNameMismatchSignal extends DependencyCollectFailedSignalBase
{
    private final String exceptedDependencyName;

    public DependencyNameMismatchSignal(String actualDependencyName, String exceptedDependencyName)
    {
        super(actualDependencyName);
        this.exceptedDependencyName = exceptedDependencyName;
    }

    public String getExceptedDependencyName()
    {
        return this.exceptedDependencyName;
    }

    public String getActualDependencyName()
    {
        return getFailedDependency();
    }
}
