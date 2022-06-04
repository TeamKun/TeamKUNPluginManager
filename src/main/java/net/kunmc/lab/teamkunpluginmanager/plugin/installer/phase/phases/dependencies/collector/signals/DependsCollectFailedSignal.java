package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.collector.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;

import java.util.List;

@Value
public class DependsCollectFailedSignal implements InstallerSignal
{
    List<String> collectFailedPlugins;
}