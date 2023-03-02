package org.kunlab.kpm.task.interfaces.dependencies.collector.signals;

/**
 * 依存関係の解決に失敗したことを示すシグナルです。
 */
public class DependencyResolveFailedSignal extends DependencyCollectFailedSignalBase
{
    public DependencyResolveFailedSignal(String failedDependency)
    {
        super(failedDependency);
    }
}
