package net.kunmc.lab.kpm.installer.task.tasks.uninstall;

/**
 * アンインストールに失敗した理由を表します。
 */
public enum UninstallErrorCause
{
    /**
     * いくつかのプラグインのアンインストールに失敗しました。
     */
    SOME_UNINSTALL_FAILED,

    /**
     * 内部：1つのプラグインのアンインストールに成功しました。
     */
    INTERNAL_UNINSTALL_OK,
    /**
     * 內部：１つのプラグインの無効化に成功し、アンインストール処理を完了しました。
     */
    INTERNAL_DISABLE_AND_UNINSTALL_OK,

    /**
     * 内部：1つのプラグインの無効化に失敗しました。
     */
    INTERNAL_PLUGIN_DISABLE_FAILED,
    /**
     * 内部：1つのプラグインのクラスのアンロードに失敗しました。
     */
    INTERNAL_CLASS_UNLOAD_FAILED,
}
