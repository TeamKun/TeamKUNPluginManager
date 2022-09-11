package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.Signal;

import java.util.List;

/**
 * 依存関係が列挙されたことを示すシグナルです。
 */
@AllArgsConstructor
@Value
public class DependsEnumeratedSignal implements Signal
{
    /**
     * 列挙された依存関係。
     */
    List<String> dependencies;
    /**
     * 既にサーバーにインストールされている依存関係。
     */
    List<String> ignoredDependencies;  // it means already installed or already collected
}
