package org.kunlab.kpm.task.tasks.install;

/**
 * プラグインのインストールの状態を表します。
 */
public enum PluginsInstallState
{
    /**
     * プラグインのインストールが初期化されたことを示します。
     */
    INITIALIZED,

    /**
     * プラグインの再配置中であることを示します。
     */
    PLUGIN_RELOCATING,
    /**
     * プラグインの読み込み中であることを示します。
     */
    PLUGIN_LOADING,
    /**
     * プラグインの{@link org.bukkit.plugin.Plugin#onLoad()} の実行中であることを示します。
     */
    ONLOAD_RUNNING,
    /**
     * プラグインの有効化中であることを示します。
     */
    PLUGIN_ENABLING,
}
