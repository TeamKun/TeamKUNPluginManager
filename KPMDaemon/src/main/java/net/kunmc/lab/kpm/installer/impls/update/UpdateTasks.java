package net.kunmc.lab.kpm.installer.impls.update;

/**
 * エイリアスのアップデートのタスクを表す列挙型です。
 */
public enum UpdateTasks
{
    /**
     * ソースのダウンロードを行います。
     */
    DOWNLOADING_SOURCES,
    /**
     * エイリアスのアップデートを行います。
     */
    UPDATING_ALIASES,
}
