package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadErrorCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
public class DownloadErrorSignal implements InstallerSignal
{
    @NotNull
    DownloadErrorCause cause;
    @NotNull
    String downloadId;
    @Nullable
    Object value;
}
