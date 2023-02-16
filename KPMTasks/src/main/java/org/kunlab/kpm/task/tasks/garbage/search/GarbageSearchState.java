package org.kunlab.kpm.task.tasks.garbage.search;

/**
 * 不要なデータの検索の状態を表します。
 */
public enum GarbageSearchState
{
    /**
     * 不要なデータの検索が初期化されたことを示します。
     */
    INITIALIZED,
    /**
     * 不要なデータの検索中であることを示します。
     */
    SEARCHING_GARBAGE,
}
