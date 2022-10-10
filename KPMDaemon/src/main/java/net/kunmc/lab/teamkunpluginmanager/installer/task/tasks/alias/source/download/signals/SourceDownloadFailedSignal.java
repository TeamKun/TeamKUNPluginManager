package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.alias.source.download.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.download.DownloadResult;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;

/**
 * ソースファイルのダウンロードに失敗したことを示すシグナルです。
 */
@Value
public class SourceDownloadFailedSignal implements Signal
{
    String remoteName;
    DownloadResult downloadResult;
}
