package net.kunmc.lab.kpm.installer.task.tasks.uninstall.hard.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.hard.UnInstallErrorCause;
import net.kunmc.lab.kpm.signal.Signal;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * プラグインのアンインストール中にエラーが発生したことを通知するシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class PluginUninstallErrorSignal extends Signal
{
    /**
     * エラーの原因です。
     */
    UnInstallErrorCause cause;
    /**
     * エラーが発生したプラグインのプラグイン情報ファイルです。
     */
    PluginDescriptionFile description;
}
