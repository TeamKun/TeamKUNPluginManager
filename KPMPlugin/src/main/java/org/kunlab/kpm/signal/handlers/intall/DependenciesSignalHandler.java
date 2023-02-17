package org.kunlab.kpm.signal.handlers.intall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.task.tasks.dependencies.collector.signals.DependencyCollectDependencysDependsFailedSignal;
import org.kunlab.kpm.task.tasks.dependencies.collector.signals.DependencyLoadDescriptionFailedSignal;
import org.kunlab.kpm.task.tasks.dependencies.collector.signals.DependencyNameMismatchSignal;
import org.kunlab.kpm.task.tasks.dependencies.collector.signals.DependencyResolveFailedSignal;
import org.kunlab.kpm.task.tasks.dependencies.computer.signals.DependsLoadOrderComputingSignal;

/**
 * 依存関係のシグナルを処理するハンドラです.
 */
public class DependenciesSignalHandler
{
    private final Terminal terminal;

    public DependenciesSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    // Ignore DependsEnumeratedSignal
    // Ignore other resolves
    // Ignore other downloads
    // Ignore DependencyDownloadFailedSignal
    // Ignore DependsLoadOrderComputingSignal.Pre

    @SignalHandler
    public void onDependencyResolveFailed(DependencyResolveFailedSignal signal)
    {
        this.terminal.error("依存関係の解決に失敗しました： %s", signal.getFailedDependency());
    }

    @SignalHandler
    public void onDepsDepsFailed(DependencyCollectDependencysDependsFailedSignal signal)
    {
        signal.getCollectFailedDependencies().forEach(dependency ->
                this.terminal.error("依存関係の取得に失敗しました： %s", dependency));
    }

    @SignalHandler
    public void onDependencyLoadDescriptionFailed(DependencyLoadDescriptionFailedSignal signal)
    {
        this.terminal.error("依存関係の読み取りに失敗しました： %s", signal.getFailedDependency());
    }

    @SignalHandler
    public void onDependencyNameMismatch(DependencyNameMismatchSignal signal)
    {
        this.terminal.error("依存関係の整合性が確認できませんでした： %s", signal.getFailedDependency());
        this.terminal.info("他のバージョンのプラグインを使用することで解決できる可能性があります。");
    }

    @SignalHandler
    public void onDependencyTreeBuilt(DependsLoadOrderComputingSignal.Pre signal)
    {
        this.terminal.info("依存関係ツリーを構築中 …");
    }
}