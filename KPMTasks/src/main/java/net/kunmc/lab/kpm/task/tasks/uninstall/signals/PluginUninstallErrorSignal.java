package net.kunmc.lab.kpm.task.tasks.uninstall.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.signal.Signal;
import net.kunmc.lab.kpm.task.tasks.uninstall.UninstallErrorCause;
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
    UninstallErrorCause cause;
    /**
     * エラーが発生したプラグインのプラグイン情報ファイルです。
     */
    PluginDescriptionFile description;
}