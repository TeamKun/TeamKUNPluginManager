package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.dependencies.collector.signals;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * 依存関係のダウンロードに失敗したことを示すシグナルです。
 */
public class DependencyDownloadFailedSignal extends DependencyCollectFailedSignalBase
{
    /**
     * ダウンロード元のURLです。
     */
    @NotNull
    @Getter
    private final String url;

    public DependencyDownloadFailedSignal(@NotNull String failedDependency, @NotNull String url)
    {
        super(failedDependency);

        this.url = url;
    }
}
