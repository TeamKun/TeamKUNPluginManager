package net.kunmc.lab.kpm.installer.task.tasks.uninstall;

/**
 * アンインストールの状態を表します。
 */
public enum UninstallState
{
    /**
     * アンインストールが初期化されたことを示します。
     */
    INITIALIZED,
    /**
     * アンインストールが開始されたことを示します。
     */
    UNINSTALLING,

    /**
     * レシピの登録解除中であることを示します。
     */
    RECIPES_UNREGISTERING,
    /**
     * コマンドのアンパッチ中であることを示します。
     */
    COMMANDS_UNPATCHING,
    /**
     * プラグインの無効化中であることを示します。
     */
    PLUGIN_DISABLING,

    /**
     * Bukkit からプラグインをアンロード中であることを示します。
     */
    REMOVING_FROM_BUKKIT,
    /**
     * クラスのアンロード中であることを示します。
     */
    CLASSES_UNLOADING
}
