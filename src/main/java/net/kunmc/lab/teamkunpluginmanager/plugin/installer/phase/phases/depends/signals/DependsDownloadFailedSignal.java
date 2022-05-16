package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals;

public class DependsDownloadFailedSignal extends DependencyCollectFailedSignalBase
{
    public DependsDownloadFailedSignal(String failedDependency)
    {
        super(failedDependency);
    }
}
