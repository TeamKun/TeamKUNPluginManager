package net.kunmc.lab.kpm.installer.task.tasks.garbage.search;

/**
 * 不要データ検索の状態を表します。
 */
public enum GarbageSearchState
{
    /**
     * 不要データ検索が初期化されたことを示します。
     */
    INITIALIZED,
    /**
     * 不要データの検索中であることを示します。
     */
    SEARCHING_GARBAGE,
}
