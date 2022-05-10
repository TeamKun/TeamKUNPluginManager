package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Value
public class DownloadSucceedSignal implements InstallerSignal
{
    @NotNull
    String downloadId;

    @NotNull
    Path downloadPath;
    long totalSize;
}
