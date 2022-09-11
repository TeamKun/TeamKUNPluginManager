package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.intall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals.DependencyCollectDependencysDependsFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals.DependencyLoadDescriptionFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals.DependencyNameMismatchSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals.DependencyResolveFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.computer.signals.DependsLoadOrderComputingSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandler;
import org.bukkit.ChatColor;

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
        terminal.error("依存関係の解決に失敗しました: " + signal.getFailedDependency());
    }

    @SignalHandler
    public void onDepsDepsFailed(DependencyCollectDependencysDependsFailedSignal signal)
    {
        signal.getCollectFailedDependencies().forEach(dependency ->
                terminal.error("依存関係の取得に失敗しました: " + dependency));
    }

    @SignalHandler
    public void onDependencyLoadDescriptionFailed(DependencyLoadDescriptionFailedSignal signal)
    {
        terminal.error("依存関係の読み取りに失敗しました: " + signal.getFailedDependency());
    }

    @SignalHandler
    public void onDependencyNameMismatch(DependencyNameMismatchSignal signal)
    {
        terminal.error("依存関係の整合性が確認できませんでした: " + signal.getFailedDependency());
    }

    @SignalHandler
    public void onDependencyTreeBuilt(DependsLoadOrderComputingSignal.Pre signal)
    {
        terminal.writeLine(ChatColor.GREEN + "依存関係ツリーを構築中 ...");
    }
}
