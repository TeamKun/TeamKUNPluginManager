package org.kunlab.kpm.task.tasks.dependencies.collector;

/**
 * 依存関係取得の状態を表す列挙型です。
 */
public enum DependsCollectState
{
    /**
     * 依存関係の取得が初期化されたことを示します。
     */
    INITIALIZED,

    /**
     * 依存関係の解決中であることを示します。
     */
    RESOLVING_DEPENDS,
    /**
     * 依存関係のダウンロード中であることを示します。
     */
    DOWNLOADING_DEPENDS,

    /**
     * 依存関係の依存関係が検出された場合、再帰的に解決していることを示します。
     */
    COLLECTING_DEPENDS_DEPENDS // Dependency's depends.
}
