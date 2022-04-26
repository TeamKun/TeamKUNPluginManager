package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.download;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignal;

import java.nio.file.Path;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class DownloadStartedSignal extends DownloadSignal implements InstallerSignal
{
    private Path downloadPath;
    private String url;

    public DownloadStartedSignal(UUID downloadId, Path downloadPath, String url)
    {
        super(downloadId);
        this.downloadPath = downloadPath;
        this.url = url;
    }
}
