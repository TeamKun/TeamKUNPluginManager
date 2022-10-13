package net.kunmc.lab.teamkunpluginmanager.installer.impls.autoremove.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.uninstall.UnInstallTasks;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;

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
