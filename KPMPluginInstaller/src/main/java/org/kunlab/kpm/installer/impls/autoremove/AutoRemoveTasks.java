package org.kunlab.kpm.installer.impls.autoremove;

/**
 * 自動削除のタスクを表す列挙型です。
 */
public enum AutoRemoveTasks
{
    /**
     * 削除対象のプラグインを取得します。
     */
    SEARCHING_REMOVABLES,
    /**
     * 削除対象のプラグインを削除します。
     */
    UNINSTALLING_PLUGINS
}
