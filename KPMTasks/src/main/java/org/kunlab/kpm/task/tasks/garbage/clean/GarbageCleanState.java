package org.kunlab.kpm.task.tasks.garbage.clean;

/**
 * 不要なデータの削除状態を表します。
 */
public enum GarbageCleanState
{
    /**
     * 不要なデータの削除が初期化されたことを示します。
     */
    INITIALIZED,
    /**
     * 不要なデータをの削除中であることを示します。
     */
    DELETING_GARBAGE,
}
