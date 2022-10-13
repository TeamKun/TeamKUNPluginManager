package net.kunmc.lab.teamkunpluginmanager.meta;

/**
 * プラグインが誰によってインストールされたかを表します。
 */
public enum InstallOperator
{
    /**
     * サーバの管理者によって手動でインストールされたことを表します。
     */
    SERVER_ADMIN,
    /**
     * プラグイン依存関係リゾルバによって自動でインストールされたことを表します。
     */
    KPM_DEPENDENCY_RESOLVER,
    /**
     * プラグインアップデータによって自動でインストールされたことを表します。
     */
    KPM_PLUGIN_UPDATER,
    /**
     * その他の方法でインストールされたことを表します。
     */
    OTHER,
    /**
     * 未知の方法でインストールされたことを表します。
     */
    UNKNOWN
}
