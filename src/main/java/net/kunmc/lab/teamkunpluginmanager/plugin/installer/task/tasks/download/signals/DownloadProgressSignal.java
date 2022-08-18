package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
@EqualsAndHashCode(callSuper = true)
public class DownloadProgressSignal extends DownloadSignal
{
    long totalSize;
    long downloaded;

    double percentage;

    public DownloadProgressSignal(@NotNull String downloadId, long totalSize, long downloaded, double percentage)
    {
        super(downloadId);
        this.totalSize = totalSize;
        this.downloaded = downloaded;
        this.percentage = percentage;
    }
}
