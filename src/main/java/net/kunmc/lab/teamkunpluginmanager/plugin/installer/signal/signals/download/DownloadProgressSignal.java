package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.download;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignal;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Value
public class DownloadProgressSignal extends DownloadSignal implements InstallerSignal
{
    long totalSize;
    long downloaded;

    double percentage;

    public DownloadProgressSignal(UUID downloadId, long totalSize, long downloaded, double percentage)
    {
        super(downloadId);
        this.totalSize = totalSize;
        this.downloaded = downloaded;
        this.percentage = percentage;
    }
}
