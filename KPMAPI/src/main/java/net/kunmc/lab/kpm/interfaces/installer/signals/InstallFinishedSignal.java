package net.kunmc.lab.kpm.interfaces.installer.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.interfaces.installer.InstallResult;
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
    InstallResult<? extends Enum<?>> result;
}
