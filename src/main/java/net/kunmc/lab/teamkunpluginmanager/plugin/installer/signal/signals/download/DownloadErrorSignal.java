package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.download;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignal;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Value
public class DownloadErrorSignal extends DownloadSignal implements InstallerSignal
{
    DownloadErrorCause cause;
    Object value;

    public DownloadErrorSignal(UUID downloadId, DownloadErrorCause cause, Object value)
    {
        super(downloadId);
        this.cause = cause;
        this.value = value;
    }

}
