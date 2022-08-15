package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Data
public class PluginRelocatingSignal implements InstallerSignal
{
    @NotNull
    private final Path source;
    @NotNull
    private Path target;
}
