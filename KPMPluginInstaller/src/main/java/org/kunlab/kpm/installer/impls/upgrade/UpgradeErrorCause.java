package org.kunlab.kpm.installer.impls.upgrade;

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
     * KPM 自体のアップグレードをしようとしました。
     * また、他にアップグレードできるプラグインも存在しません。
     */
    SELF_UPGRADE_ATTEMPTED,
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
     * アップグレードできるプラグインが見つかりませんでした。
     * このエラーは, 明確なエラーではない可能性があります。
     */
    UP_TO_DATE,
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
    /**
     * 依存関係の読み込み順序計算に失敗しました。
     */
    DEPENDENCY_LOAD_ORDER_COMPUTE_FAILED,
}
