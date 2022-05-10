package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Value
public class DependsDownloadFailedSignal implements InstallerSignal
{
    @NotNull
    List<String> downloadFailedDependencies;
}
