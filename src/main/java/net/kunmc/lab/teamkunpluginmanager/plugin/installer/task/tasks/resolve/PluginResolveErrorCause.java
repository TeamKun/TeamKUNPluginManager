package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve;

/**
 * プラグインの解決に失敗した理由を表します。
 */
public enum PluginResolveErrorCause
{
    /**
     * 予期しない内部エラーが発生したことを示します。
     */
    ILLEGAL_INTERNAL_STATE,
    /**
     * {@link net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult} が サーバまたはプラグインリゾルバから返されたことを示します。
     */
    GOT_ERROR_RESULT
}
