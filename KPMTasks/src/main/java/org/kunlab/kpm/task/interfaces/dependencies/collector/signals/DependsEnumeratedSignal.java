package org.kunlab.kpm.task.interfaces.dependencies.collector.signals;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.kunlab.kpm.resolver.QueryContext;
import org.kunlab.kpm.signal.Signal;

import java.util.List;
import java.util.Map;

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
    Map<String, QueryContext> dependencies;
    /**
     * 既にサーバーにインストールされている依存関係。
     */
    List<String> ignoredDependencies;  // it means already installed or already collected
}
