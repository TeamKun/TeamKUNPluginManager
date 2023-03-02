package org.kunlab.kpm.task.tasks.alias.source.download.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.kunlab.kpm.signal.Signal;
import org.kunlab.kpm.task.tasks.download.DownloadResult;

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
