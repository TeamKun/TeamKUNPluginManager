package net.kunmc.lab.kpm.installer.impls.autoremove.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.installer.impls.uninstall.UnInstallTasks;
import net.kunmc.lab.kpm.interfaces.installer.InstallResult;
import net.kunmc.lab.kpm.signal.Signal;

/**
 * アンインストールに失敗したときに送信されるシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class PluginUninstallFailedSignal extends Signal
{
    /**
     * アンインストールに失敗したときの結果です。
     */
    InstallResult<UnInstallTasks> result;
}
