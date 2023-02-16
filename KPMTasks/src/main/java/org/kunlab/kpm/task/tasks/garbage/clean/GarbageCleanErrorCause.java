package org.kunlab.kpm.task.tasks.garbage.clean;

/**
 * 不要なデータの削除エラーを表す列挙型です。
 */
public enum GarbageCleanErrorCause
{
    /**
     * キャンセルされました。
     */
    CANCELLED,
    /**
     * 不要なデータが存在しません。
     */
    NO_GARBAGE,

    /**
     * すべての削除に失敗しました。
     */
    ALL_DELETE_FAILED,
    /**
     * ファイル・システムとの整合性が取れなく、スキップされました。
     */
    INVALID_INTEGRITY,
}
