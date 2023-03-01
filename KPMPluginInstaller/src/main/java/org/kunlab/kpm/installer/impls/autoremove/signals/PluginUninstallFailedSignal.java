package org.kunlab.kpm.installer.impls.autoremove.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.kunlab.kpm.installer.impls.uninstall.UnInstallTasks;
import org.kunlab.kpm.installer.interfaces.InstallResult;
import org.kunlab.kpm.signal.Signal;

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
