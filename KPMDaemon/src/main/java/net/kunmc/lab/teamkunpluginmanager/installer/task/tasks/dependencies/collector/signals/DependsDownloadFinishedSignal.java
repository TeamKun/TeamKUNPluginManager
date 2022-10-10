package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.dependencies.collector.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.download.DownloadResult;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * 依存関係のダウンロードがすべて完了したことを示すシグナルです。
 */
@Value
public class DependsDownloadFinishedSignal implements Signal
{
    /**
     * ダウンロード結果です。
     */
    @NotNull
    HashMap<String, DownloadResult> downloads;
}
