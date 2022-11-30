package net.kunmc.lab.kpm.installer.task.tasks.dependencies.collector.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.installer.task.tasks.download.DownloadResult;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

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
