package net.kunmc.lab.kpm.installer.task.tasks.alias.source.download.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.installer.task.tasks.download.DownloadResult;
import net.kunmc.lab.kpm.signal.Signal;

/**
 * ソースファイルのダウンロードに失敗したことを示すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class SourceDownloadFailedSignal extends Signal
{
    String remoteName;
    DownloadResult downloadResult;
}
