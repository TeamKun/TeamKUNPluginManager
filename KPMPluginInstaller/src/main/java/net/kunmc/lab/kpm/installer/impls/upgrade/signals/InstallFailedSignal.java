package net.kunmc.lab.kpm.installer.impls.upgrade.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.installer.impls.install.InstallTasks;
import net.kunmc.lab.kpm.interfaces.installer.InstallResult;
import net.kunmc.lab.kpm.signal.Signal;

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
