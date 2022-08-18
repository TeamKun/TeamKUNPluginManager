package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Value
public class PluginInstallingSignal implements InstallerSignal
{
    @NotNull
    Path path;
    @NotNull
    PluginDescriptionFile pluginDescription;
}
