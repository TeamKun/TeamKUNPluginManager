package net.kunmc.lab.teamkunpluginmanager.plugin.installer.install.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;

import java.util.List;

@Value
public class DependenciesCollectFailedSignal implements InstallerSignal
{
    List<String> collectFailedPlugins;
}
