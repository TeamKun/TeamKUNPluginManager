package org.kunlab.kpm.task.interfaces.dependencies.collector.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;
import org.kunlab.kpm.task.tasks.download.DownloadResult;

import java.util.HashMap;

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
    HashMap<String, DownloadResult> downloads;
}
