package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.download;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignal;

import java.nio.file.Path;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Value
public class DownloadSucceedSignal extends DownloadSignal implements InstallerSignal
{
    Path downloadPath;
    long totalSize;

    public DownloadSucceedSignal(UUID downloadId, Path downloadPath, long totalSize)
    {
        super(downloadId);
        this.downloadPath = downloadPath;
        this.totalSize = totalSize;
    }
}
