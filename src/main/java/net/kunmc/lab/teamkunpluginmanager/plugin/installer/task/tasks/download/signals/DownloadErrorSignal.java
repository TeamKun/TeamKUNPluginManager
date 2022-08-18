package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.DownloadErrorCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
@EqualsAndHashCode(callSuper = true)
public class DownloadErrorSignal extends DownloadSignal
{
    @NotNull
    DownloadErrorCause cause;
    @Nullable
    Object value;

    public DownloadErrorSignal(@NotNull String downloadId, @NotNull DownloadErrorCause cause, @Nullable Object value)
    {
        super(downloadId);
        this.cause = cause;
        this.value = value;
    }
}
