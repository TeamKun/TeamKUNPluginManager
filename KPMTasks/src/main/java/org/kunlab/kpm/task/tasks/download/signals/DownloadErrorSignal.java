package org.kunlab.kpm.task.tasks.download.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.task.tasks.download.DownloadErrorCause;

/**
 * ダウンロードに失敗したことを示すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class DownloadErrorSignal extends DownloadSignal
{
    /**
     * 失敗したURLです。
     */
    @NotNull
    String url;

    /**
     * 失敗した理由です。
     */
    @NotNull
    DownloadErrorCause cause;
    /**
     * 失敗した理由の詳細です。
     * 失敗した原因が、
     * {@link DownloadErrorCause#ILLEGAL_HTTP_RESPONSE} 出会った場合は、{@code 404 Not Found} のような{@link String}が、
     * {@link DownloadErrorCause#IO_EXCEPTION} 出会った場合は {@link Exception}が格納されます。
     */
    @Nullable
    Object value;

    public DownloadErrorSignal(@NotNull String downloadId, @NotNull String url, @NotNull DownloadErrorCause cause, @Nullable Object value)
    {
        super(downloadId);
        this.url = url;
        this.cause = cause;
        this.value = value;
    }
}
