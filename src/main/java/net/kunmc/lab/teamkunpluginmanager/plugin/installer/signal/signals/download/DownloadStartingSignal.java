package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.download;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignal;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Data
public class DownloadStartingSignal implements InstallerSignal
{
    @NotNull
    private Path downloadPath;
    @NotNull
    private String url;
}
