package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals;

/**
 * 依存関係のダウンロードに失敗したことを示すシグナルです。
 */
public class DependencyDownloadFailedSignal extends DependencyCollectFailedSignalBase
{
    public DependencyDownloadFailedSignal(String failedDependency)
    {
        super(failedDependency);
    }
}
