package net.kunmc.lab.kpm.installer.task.tasks.resolve;

import net.kunmc.lab.kpm.resolver.result.ErrorResultImpl;

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
     * {@link ErrorResultImpl} が サーバまたはプラグインリゾルバから返されたことを示します。
     */
    GOT_ERROR_RESULT,
    /**
     * ユーザがキャンセルしたことを示します。
     */
    CANCELLED,
}
