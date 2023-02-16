package org.kunlab.kpm.task.tasks.dependencies.collector.signals;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.kunlab.kpm.signal.Signal;

/**
 * 依存関係の取得に失敗したことを示すシグナルです。
 * このシグナルは基礎的なものであり、具体的な失敗の理由はサブクラスによって表現されます。
 */
@AllArgsConstructor
@Getter
public abstract class DependencyCollectFailedSignalBase extends Signal
{
    /**
     * 取得に失敗した依存関係の名前です。
     */
    private final String failedDependency;
}
