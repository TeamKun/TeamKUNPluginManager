package net.kunmc.lab.kpm.installer.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.installer.InstallResult;
import net.kunmc.lab.kpm.signal.Signal;

/**
 * インストールが完了したことを表すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class InstallFinishedSignal extends Signal
{
    /**
     * インストール結果
     */
    InstallResult<?> result;
}
