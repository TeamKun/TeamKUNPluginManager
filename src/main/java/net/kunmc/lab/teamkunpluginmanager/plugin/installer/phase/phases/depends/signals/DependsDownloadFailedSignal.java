package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.jetbrains.annotations.NotNull;

@Value
public class DependsDownloadFailedSignal implements InstallerSignal
{
    @NotNull
    String downloadFailedDependency;
}
