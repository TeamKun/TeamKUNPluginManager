package net.kunmc.lab.kpm.task.tasks.dependencies.collector.signals;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.signal.Signal;

import java.util.List;

/**
 * 依存関係が列挙されたことを示すシグナルです。
 */
@AllArgsConstructor
@Value
@EqualsAndHashCode(callSuper = false)
public class DependsEnumeratedSignal extends Signal
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
