package org.kunlab.kpm.signal.handlers.intall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.task.interfaces.dependencies.collector.signals.DependencyCollectDependencysDependsFailedSignal;
import org.kunlab.kpm.task.interfaces.dependencies.collector.signals.DependencyLoadDescriptionFailedSignal;
import org.kunlab.kpm.task.interfaces.dependencies.collector.signals.DependencyNameMismatchSignal;
import org.kunlab.kpm.task.interfaces.dependencies.collector.signals.DependencyResolveFailedSignal;
import org.kunlab.kpm.task.interfaces.dependencies.computer.signals.DependsLoadOrderComputingSignal;

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
        this.terminal.error(LangProvider.get(
                "tasks.deps.resolve.failed",
                MsgArgs.of("dependency", signal.getFailedDependency())
        ));
    }

    @SignalHandler
    public void onDepsDepsFailed(DependencyCollectDependencysDependsFailedSignal signal)
    {
        signal.getCollectFailedDependencies().forEach(dependency ->
                this.terminal.error(LangProvider.get(
                        "tasks.deps.collect.failed",
                        MsgArgs.of("dependency", dependency)
                )))
        ;
    }

    @SignalHandler
    public void onDependencyLoadDescriptionFailed(DependencyLoadDescriptionFailedSignal signal)
    {
        this.terminal.error(LangProvider.get(
                "tasks.deps.load.failed",
                MsgArgs.of("dependency", signal.getFailedDependency())
        ));
    }

    @SignalHandler
    public void onDependencyNameMismatch(DependencyNameMismatchSignal signal)
    {
        this.terminal.error(LangProvider.get(
                "tasks.deps.mismatch",
                MsgArgs.of("dependency", signal.getFailedDependency())
        ));
        this.terminal.info(LangProvider.get("tasks.deps.mismatch.suggest"));
    }

    @SignalHandler
    public void onDependencyTreeBuilt(DependsLoadOrderComputingSignal.Pre signal)
    {
        this.terminal.info(LangProvider.get("tasks.deps.building_tree"));
    }
}
