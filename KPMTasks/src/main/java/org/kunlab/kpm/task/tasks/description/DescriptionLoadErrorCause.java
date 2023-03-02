package org.kunlab.kpm.task.tasks.description;

/**
 * プラグイン情報ファイルの読み込みに失敗した際のエラーの原因を表します。
 */
public enum DescriptionLoadErrorCause
{
    /**
     * 指定されたファイルがプラグインではないか、 plugin.yml が存在しません。
     */
    NOT_A_PLUGIN,
    /**
     * 不正なプラグイン情報ファイルです。
     */
    INVALID_DESCRIPTION,
    /**
     * {@link java.io.IOException} が発生しました。
     */
    IO_EXCEPTION
}
