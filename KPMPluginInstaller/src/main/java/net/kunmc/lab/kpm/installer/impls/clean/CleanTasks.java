package net.kunmc.lab.kpm.installer.impls.clean;

/**
 * 不要データ削除のタスクを表す列挙型です。
 */
public enum CleanTasks
{
    /**
     * 不要データを検索します。
     */
    SEARCHING_GARBAGE,
    /**
     * 不要データの削除中です。
     */
    DELETING_GARBAGE,
}