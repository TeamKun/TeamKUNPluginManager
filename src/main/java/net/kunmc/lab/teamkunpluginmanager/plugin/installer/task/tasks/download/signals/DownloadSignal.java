package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.jetbrains.annotations.NotNull;

@Data
public class DownloadSignal implements InstallerSignal
{
    @NotNull
    private final String downloadId;
}
