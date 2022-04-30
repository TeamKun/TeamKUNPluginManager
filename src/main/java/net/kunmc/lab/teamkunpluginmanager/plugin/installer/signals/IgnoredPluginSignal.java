package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.jetbrains.annotations.NotNull;

@Value
public class IgnoredPluginSignal implements InstallerSignal
{
    @NotNull
    String pluginName;
}
