package org.kunlab.kpm.task.tasks.alias.update;

/**
 * エイリアスのアップデートの状態を表します。
 */
public enum UpdateAliasesState
{
    /**
     * エイリアスのアップデートが初期化されたことを示します。
     */
    INITIALIZED,
    /**
     * エイリアスのアップデート中であることを示します。
     */
    UPDATING,
}
