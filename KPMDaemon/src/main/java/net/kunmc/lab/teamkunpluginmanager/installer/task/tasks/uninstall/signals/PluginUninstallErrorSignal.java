package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.uninstall.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.uninstall.UnInstallErrorCause;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * プラグインのアンインストール中にエラーが発生したことを通知するシグナルです。
 */
@Value
public class PluginUninstallErrorSignal implements Signal
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
