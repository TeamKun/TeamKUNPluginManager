package net.kunmc.lab.kpm.installer.impls.autoremove;

/**
 * 自動削除のエラーを表す列挙型です。
 */
public enum AutoRemoveErrorCause
{
    /**
     * 自動削除が可能なプラグインが見つかりませんでした。
     */
    NO_AUTO_REMOVABLE_PLUGIN_FOUND,

    /**
     * アンインストーラの初期化に失敗しました。
     */
    UNINSTALLER_INIT_FAILED,
    /**
     * アンインストールに失敗しました。
     */
    UNINSTALL_FAILED,
    /**
     * 自動削除がキャンセルされました。
     */
    CANCELLED,
}
