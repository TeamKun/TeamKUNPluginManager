package net.kunmc.lab.kpm.installer.impls.upgrade;

/**
 * アップグレードのエラーを表す列挙型です。
 */
public enum UpgradeErrorCause
{
    /**
     * 指定されたプラグインが見つかりませんでした。
     */
    PLUGIN_NOT_FOUND,
    /**
     * プラグインの解決に失敗しました。
     */
    PLUGIN_RESOLVE_FAILED,
    /**
     * プラグインのバージョンが定義されていません。
     */
    PLUGIN_VERSION_NOT_DEFINED,
    /**
     * プラグインが定義してるバージョンの形式が不正です。
     */
    PLUGIN_VERSION_FORMAT_MALFORMED,
    /**
     * プラグインのバージョンがサーバのバージョンと変わらないか、古いです。
     */
    PLUGIN_IS_OLDER_OR_EQUAL,
    /**
     * アップグレードがキャンセルされました。
     */
    CANCELLED,
    /**
     * プラグインが構成等で除外されています。
     */
    PLUGIN_EXCLUDED,
    /**
     * アンインストーラのインスタンス化に失敗しました。
     */
    UNINSTALLER_INSTANTIATION_FAILED,
    /**
     * アンインストールに失敗しました。
     */
    UNINSTALL_FAILED,
    /**
     * インストーラのインスタンス化に失敗しました。
     */
    INSTALLER_INSTANTIATION_FAILED,
    /**
     * インストールに失敗しました。
     */
    INSTALL_FAILED,
}
