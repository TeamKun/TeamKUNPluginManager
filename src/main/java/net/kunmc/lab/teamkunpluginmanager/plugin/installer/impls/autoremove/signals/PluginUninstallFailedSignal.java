package net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.autoremove.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall.UnInstallTasks;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.Signal;

/**
 * アンインストールに失敗したときに送信されるシグナルです。
 */
@Value
public class PluginUninstallFailedSignal implements Signal
{
    /**
     * アンインストールに失敗したときの結果です。
     */
    InstallResult<UnInstallTasks> result;
}
