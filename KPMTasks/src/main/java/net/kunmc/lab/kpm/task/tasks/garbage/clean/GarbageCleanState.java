package net.kunmc.lab.kpm.task.tasks.garbage.clean;

/**
 * 不要データ削除の状態を表します。
 */
public enum GarbageCleanState
{
    /**
     * 不要データ削除が初期化されたことを示します。
     */
    INITIALIZED,
    /**
     * 不要データを削除中であることを示します。
     */
    DELETING_GARBAGE,
}
