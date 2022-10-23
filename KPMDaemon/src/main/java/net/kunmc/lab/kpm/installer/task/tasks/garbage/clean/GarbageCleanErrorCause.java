package net.kunmc.lab.kpm.installer.task.tasks.garbage.clean;

/**
 * 不要データ削除のエラーを表す列挙型です。
 */
public enum GarbageCleanErrorCause
{
    /**
     * キャンセルされました。
     */
    CANCELLED,
    /**
     * 不要データが存在しません。
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
