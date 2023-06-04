package org.kunlab.kpm.utils.interfaces;

/**
 * サーバの状態を確認します。
 */
public interface ServerConditionChecker
{
    /**
     * サーバが終了中かどうか取得します。
     *
     * @return サーバが終了中かどうか
     */
    boolean isStopping();

    /**
     * サーバがリロード中かどうか取得します。
     *
     * @return サーバがリロード中かどうか
     */
    boolean isReloading();
}
