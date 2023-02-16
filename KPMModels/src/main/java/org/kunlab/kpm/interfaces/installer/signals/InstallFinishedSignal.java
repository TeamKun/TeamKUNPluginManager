package org.kunlab.kpm.interfaces.installer.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.kunlab.kpm.interfaces.installer.InstallResult;
import org.kunlab.kpm.signal.Signal;

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
