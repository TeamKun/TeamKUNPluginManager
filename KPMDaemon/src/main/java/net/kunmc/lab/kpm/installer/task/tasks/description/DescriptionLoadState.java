package net.kunmc.lab.kpm.installer.task.tasks.description;

/**
 * プラグイン情報ファイルの読み込みの状態を表します。
 */
public enum DescriptionLoadState
{
    /**
     * プラグイン情報ファイルの読み込みが初期化されたことを示します。
     */
    INITIALIZED,

    /**
     * プラグイン情報ファイルの読み込み中であることを示します。
     */
    LOADING_PLUGIN_DESCRIPTION
}
