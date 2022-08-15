package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.jetbrains.annotations.NotNull;

@Value
public class DownloadProgressSignal implements InstallerSignal
{
    @NotNull
    String downloadId;

    long totalSize;
    long downloaded;

    double percentage;
}
