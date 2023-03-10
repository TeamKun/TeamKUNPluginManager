package org.kunlab.kpm.task.tasks.dependencies.collector.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;
import org.kunlab.kpm.task.tasks.download.DownloadResult;

import java.util.Map;

/**
 * 依存関係のダウンロードがすべて完了したことを示すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class DependsDownloadFinishedSignal extends Signal
{
    /**
     * ダウンロード結果です。
     */
    @NotNull
    Map<String, DownloadResult> downloads;
}
