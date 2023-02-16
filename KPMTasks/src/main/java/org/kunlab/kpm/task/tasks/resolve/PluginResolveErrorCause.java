package org.kunlab.kpm.task.tasks.resolve;

import org.kunlab.kpm.interfaces.resolver.result.ErrorResult;

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
     * {@link ErrorResult} が サーバまたはプラグインリゾルバから返されたことを示します。
     */
    GOT_ERROR_RESULT,
    /**
     * ユーザがキャンセルしたことを示します。
     */
    CANCELLED,
}
