package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;

@Value
public class DownloadProgressSignal implements InstallerSignal
{
    long totalSize;
    long downloaded;

    double percentage;
}
