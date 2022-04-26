package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.download;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignal;

@Value
public class DownloadProgressSignal implements InstallerSignal
{
    long totalSize;
    long downloaded;

    double percentage;
}
