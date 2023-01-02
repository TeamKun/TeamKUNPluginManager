package net.kunmc.lab.kpm.installer.impls.clean;

/**
 * 不要なデータの削除タスクを表す列挙型です。
 */
public enum CleanTasks
{
    /**
     * 不要なデータを検索します。
     */
    SEARCHING_GARBAGE,
    /**
     * 不要なデータの削除中です。
     */
    DELETING_GARBAGE,
}
