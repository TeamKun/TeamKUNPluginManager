package net.kunmc.lab.kpm.installer.signals;

import lombok.Value;
import net.kunmc.lab.kpm.installer.InstallResult;
import net.kunmc.lab.kpm.signal.Signal;

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
