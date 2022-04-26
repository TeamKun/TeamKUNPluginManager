package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.download;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@EqualsAndHashCode
public abstract class DownloadSignal
{
    @Getter
    private final UUID downloadId;

    public DownloadSignal(UUID downloadId)
    {
        this.downloadId = downloadId;
    }
}
