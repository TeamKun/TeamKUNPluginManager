package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.DownloadResult;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * 依存関係のダウンロードがすべて完了したことを示すシグナルです。
 */
@Value
public class DependsDownloadFinishedSignal implements InstallerSignal
{
    /**
     * ダウンロード結果です。
     */
    @NotNull
    HashMap<String, DownloadResult> downloads;
}
