package org.kunlab.kpm.task.tasks.lookup;

/**
 * プラグインの検索の状態を表します。
 */
public enum LookupState
{
    /**
     * プラグインの検索が初期化されたことを示します。
     */
    INITIALIZED,
    /**
     * クエリの解析中であることを示します。
     */
    QUERY_PARSING,
    /**
     * プラグインの検索中であることを示します。
     */
    PLUGIN_LOOKUP
}
