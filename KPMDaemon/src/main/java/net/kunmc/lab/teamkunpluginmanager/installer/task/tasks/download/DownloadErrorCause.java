package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.download;

/**
 * ダウンロードに失敗した際のエラーの原因を表します。
 */
public enum DownloadErrorCause
{
    /**
     * サーバから、無効なレスポンスを受け取りました。
     */
    ILLEGAL_HTTP_RESPONSE,
    /**
     * Body が空でした。
     */
    NO_BODY_IN_RESPONSE,
    /**
     * {@link java.io.IOException} が発生しました。
     */
    IO_EXCEPTION,
    /**
     * 不明なエラーが発生しました。
     */
    UNKNOWN_ERROR
}
