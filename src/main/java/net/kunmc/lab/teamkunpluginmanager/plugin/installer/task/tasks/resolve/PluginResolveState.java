package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve;

/**
 * プラグインの解決の状態を表します。
 */
public enum PluginResolveState
{
    /**
     * プラグインの解決が初期化されたことを示します。
     */
    INITIALIZED,

    /**
     * プラグインの解決が開始されたことを示します。
     */
    PRE_RESOLVING,
    /**
     * プラグインの解決が完了したことを示します。
     */
    PRE_RESOLVE_FINISHED,

    /**
     * 複数結果があった場合、どれを選択するかの選択中であることを示します。
     */
    MULTI_RESOLVING,

    /**
     * プラグインの解決が完了したことを示します。
     */
    RESOLVE_FINISHED
}
