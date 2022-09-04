package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.DownloadErrorCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ダウンロードに失敗したことを示すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class DownloadErrorSignal extends DownloadSignal
{
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

    public DownloadErrorSignal(@NotNull String downloadId, @NotNull DownloadErrorCause cause, @Nullable Object value)
    {
        super(downloadId);
        this.cause = cause;
        this.value = value;
    }
}
