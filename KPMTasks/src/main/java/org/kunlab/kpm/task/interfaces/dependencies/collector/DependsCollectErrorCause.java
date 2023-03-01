package org.kunlab.kpm.task.interfaces.dependencies.collector;

/**
 * 依存関係の取得に失敗した理由を表します。
 */
public enum DependsCollectErrorCause
{
    /**
     * いくつかの依存関係の解決に失敗しました。
     */
    SOME_DEPENDENCIES_COLLECT_FAILED,
}
