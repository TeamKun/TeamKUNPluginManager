package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task;

/**
 * 一般的なエラーの原因を表します。
 */
public enum GeneralTaskErrorCause
{
    // General internal errors
    /**
     * 予期しないタスクの返答があリました。
     */
    ILLEGAL_INTERNAL_STATE,
    /**
     * {@link java.io.IOException} が発生しました。
     */
    IO_EXCEPTION_OCCURRED,
}
