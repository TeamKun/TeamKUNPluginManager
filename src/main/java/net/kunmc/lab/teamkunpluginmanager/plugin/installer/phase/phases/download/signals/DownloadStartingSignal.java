package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Data
public class DownloadStartingSignal implements InstallerSignal
{
    @NotNull
    String downloadId;

    @NotNull
    private Path downloadPath;
    @NotNull
    private String url;
}
