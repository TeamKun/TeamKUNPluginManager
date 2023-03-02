package org.kunlab.kpm.task.interfaces.dependencies.collector.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.kunlab.kpm.signal.Signal;

import java.util.List;

/**
 * 依存関係の取得に失敗したことを示すシグナルです。
 * 注意：このシグナルは {@link DependencyCollectFailedSignalBase} とは違い個別には送信されず、依存関係の解決タスク終了後に一度送信されます。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class DependsCollectFailedSignal extends Signal
{
    /**
     * 取得に失敗した依存関係の名前です。
     */
    List<String> collectFailedPlugins;
}
