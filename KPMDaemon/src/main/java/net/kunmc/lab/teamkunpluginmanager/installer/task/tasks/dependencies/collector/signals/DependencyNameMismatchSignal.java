package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.dependencies.collector.signals;

import lombok.Getter;

/**
 * 依存関係解決時に使用された名前と、プラグインが開示している名前が一致しなかったことを示すシグナルです。
 */
public class DependencyNameMismatchSignal extends DependencyCollectFailedSignalBase
{
    /**
     * 期待されたプラグインの名前です。
     */
    @Getter
    private final String exceptedDependencyName;

    public DependencyNameMismatchSignal(String actualDependencyName, String exceptedDependencyName)
    {
        super(actualDependencyName);
        this.exceptedDependencyName = exceptedDependencyName;
    }

    /**
     * 実際の依存関係の名前を取得します。
     *
     * @return 実際の依存関係の名前
     */
    public String getActualDependencyName()
    {
        return getFailedDependency();
    }
}
