package net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.update;

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
