package org.kunlab.kpm.task.tasks.uninstall.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.plugin.PluginDescriptionFile;
import org.kunlab.kpm.signal.Signal;
import org.kunlab.kpm.task.tasks.uninstall.UninstallErrorCause;

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
