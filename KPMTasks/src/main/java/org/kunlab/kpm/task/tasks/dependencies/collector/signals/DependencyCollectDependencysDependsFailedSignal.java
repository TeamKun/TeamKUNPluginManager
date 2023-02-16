package org.kunlab.kpm.task.tasks.dependencies.collector.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

import java.util.List;

/**
 * 依存関係の依存関係を取得する際に失敗したことを示すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class DependencyCollectDependencysDependsFailedSignal extends Signal
{
    /**
     * 対象のプラグインの名前です。
     */
    @NotNull
    String pluginName;
    /**
     * 取得に失敗した依存関係の名前です。
     */
    @NotNull
    List<String> collectFailedDependencies;
}
