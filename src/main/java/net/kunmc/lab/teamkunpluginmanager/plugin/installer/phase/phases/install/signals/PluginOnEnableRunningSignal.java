package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.install.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@Value
public class PluginOnEnableRunningSignal implements InstallerSignal
{
    @NotNull
    Plugin plugin;
}
