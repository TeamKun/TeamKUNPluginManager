package org.kunlab.kpm.installer.interfaces;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.jetbrains.annotations.NotNull;

/**
 * インストールの結果を表します。
 *
 * @param <P> インストールの進捗状況の型
 */
public interface InstallResult<P extends Enum<P>>
{
    /**
     * アップグレードされたプラグインの数を取得します。
     *
     * @return アップグレードされたプラグインの数
     */
    int getUpgradedCount();

    /**
     * インストールされたプラグインの数を取得します。
     *
     * @return インストールされたプラグインの数
     */
    int getInstalledCount();

    /**
     * 削除されたプラグインの数を取得します。
     *
     * @return 削除されたプラグインの数
     */
    int getRemovedCount();

    /**
     * 保留中としてマークされたプラグインの数を取得します。
     *
     * @return 保留中としてマークされたプラグインの数
     */
    int getPendingCount();

    /**
     * アップグレードされたプラグインの名前を取得します。
     *
     * @return アップグレードされたプラグインの名前
     */
    String[] getUpgraded();

    /**
     * インストールされたプラグインの名前を取得します。
     *
     * @return インストールされたプラグインの名前
     */
    String[] getInstalled();

    /**
     * 削除されたプラグインの名前を取得します。
     *
     * @return 削除されたプラグインの名前
     */
    String[] getRemoved();

    /**
     * 保留中としてマークされたプラグインの名前を取得します。
     *
     * @return 保留中としてマークされたプラグインの名前
     */
    String[] getPending();

    /**
     * インストールの結果をコンソールに出力します。
     *
     * @param terminal 出力先のターミナル
     */
    void printResultStatus(@NotNull Terminal terminal);

    boolean isSuccess();

    InstallProgress<P, ?> getProgress();
}
