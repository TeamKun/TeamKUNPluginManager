package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.jetbrains.annotations.NotNull;

/**
 * ダウンロード時に発生するシグナルを表す、抽象クラスです。
 */
@Data
public abstract class DownloadSignal implements InstallerSignal
{
    /**
     * ダウンロードごとに発行される一意のIDです。
     */
    @NotNull
    private final String downloadId;
}
