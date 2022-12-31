package net.kunmc.lab.kpm.task.tasks.dependencies.computer;

/**
 * 依存関係の読み込み順序計算の状態を表す列挙型です。
 */
public enum DependsComputeOrderState
{
    /**
     * 依存関係の読み込み順序計算の初期化が完了したことを示します。
     */
    INITIALIZED,

    /**
     * 依存関係マップの構築中であることを示します。
     */
    CREATING_DEPENDENCY_MAP,
    /**
     * 依存関係の読み込み順序の計算中であることを示します。
     */
    COMPUTING_DEPENDENCY_LOAD_ORDER
}
