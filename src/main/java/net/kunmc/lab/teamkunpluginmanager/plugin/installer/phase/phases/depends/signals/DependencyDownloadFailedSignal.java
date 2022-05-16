package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals;

public class DependencyDownloadFailedSignal extends DependencyCollectFailedSignalBase
{
    public DependencyDownloadFailedSignal(String failedDependency)
    {
        super(failedDependency);
    }
}
