package org.kunlab.kpm.installer.impls.upgrade.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kunlab.kpm.installer.impls.install.InstallTasks;
import org.kunlab.kpm.installer.interfaces.InstallResult;
import org.kunlab.kpm.signal.Signal;

/**
 * インストールに失敗したことを表すシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class InstallFailedSignal extends Signal
{
    /**
     * 失敗した原因の特定のためのインストール失敗結果です。
     */
    private final InstallResult<InstallTasks> failedResult;

    /**
     * このままアップグレードを続けるかどうかのフラグです。
     */
    private boolean continueUpgrade;
}
