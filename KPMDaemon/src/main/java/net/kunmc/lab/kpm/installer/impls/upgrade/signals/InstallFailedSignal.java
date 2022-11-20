package net.kunmc.lab.kpm.installer.impls.upgrade.signals;

import lombok.Data;
import net.kunmc.lab.kpm.installer.InstallResult;
import net.kunmc.lab.kpm.installer.impls.install.InstallTasks;
import net.kunmc.lab.kpm.signal.Signal;

/**
 * インストールに失敗したことを表すシグナルです。
 */
@Data
public class InstallFailedSignal implements Signal
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
