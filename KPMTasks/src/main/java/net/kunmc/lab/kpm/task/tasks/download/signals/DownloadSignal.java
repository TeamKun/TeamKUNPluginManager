package net.kunmc.lab.kpm.task.tasks.download.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

/**
 * ダウンロード時に発生するシグナルを表す、抽象クラスです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class DownloadSignal extends Signal
{
    /**
     * ダウンロードごとに発行される一意のIDです。
     */
    @NotNull
    private final String downloadId;
}
