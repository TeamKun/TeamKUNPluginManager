package net.kunmc.lab.teamkunpluginmanager.installer.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;

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
