package org.kunlab.kpm.task.interfaces.dependencies.collector.signals;

/**
 * 依存関係のプラグイン情報ファイルの読み込みに失敗したことを示すシグナルです。
 */
public class DependencyLoadDescriptionFailedSignal extends DependencyCollectFailedSignalBase
{
    public DependencyLoadDescriptionFailedSignal(String failedDependency)
    {
        super(failedDependency);
    }
}
