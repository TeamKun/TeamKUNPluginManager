package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.Signal;

/**
 * インストールが完了したことを表すシグナルです。
 */
@Value
public class InstallFinishedSignal implements Signal
{
    /**
     * インストール結果
     */
    InstallResult<?> result;
}
