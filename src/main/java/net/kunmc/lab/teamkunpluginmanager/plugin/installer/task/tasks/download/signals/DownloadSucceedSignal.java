package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Value
@EqualsAndHashCode(callSuper = true)
public class DownloadSucceedSignal extends DownloadSignal
{
    @NotNull
    Path downloadPath;
    long totalSize;

    public DownloadSucceedSignal(@NotNull String downloadId, @NotNull Path downloadPath, long totalSize)
    {
        super(downloadId);
        this.downloadPath = downloadPath;
        this.totalSize = totalSize;
    }
}
