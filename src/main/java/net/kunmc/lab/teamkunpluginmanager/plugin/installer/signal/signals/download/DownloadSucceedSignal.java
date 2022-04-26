package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.download;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignal;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Value
public class DownloadSucceedSignal implements InstallerSignal
{
    @NotNull
    Path downloadPath;
    long totalSize;

    public DownloadSucceedSignal(@NotNull Path downloadPath, long totalSize)
    {
        this.downloadPath = downloadPath;
        this.totalSize = totalSize;
    }
}
