package net.kunmc.lab.kpm.installer.task.tasks.install;

/**
 * プラグインのインストール時に発生したエラーの原因を表します。
 */
public enum PluginsInstallErrorCause
{
    /**
     * プラグインの再配置に失敗しました。
     */
    RELOCATE_FAILED,
    /**
     * 無効なプラグインが指定されました。
     */
    INVALID_PLUGIN,
    /**
     * 無効なプラグイン情報ファイルが指定されました。
     */
    INVALID_PLUGIN_DESCRIPTION,
    /**
     * プラグインがこのサーバに互換性がありません。
     */
    INCOMPATIBLE_WITH_BUKKIT_VERSION,
    /**
     * プラグインがこのKPMに互換性がありません。
     */
    INCOMPATIBLE_WITH_KPM_VERSION,

    /**
     * {@link java.io.IOException} が発生しました。
     */
    IO_EXCEPTION_OCCURRED,
    /**
     * 不明な例外が発生しました。
     */
    EXCEPTION_OCCURRED
}
