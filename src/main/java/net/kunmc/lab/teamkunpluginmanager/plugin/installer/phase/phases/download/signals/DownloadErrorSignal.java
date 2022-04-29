package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
public class DownloadErrorSignal implements InstallerSignal
{
    @NotNull
    DownloadErrorCause cause;
    @Nullable
    Object value;
}
