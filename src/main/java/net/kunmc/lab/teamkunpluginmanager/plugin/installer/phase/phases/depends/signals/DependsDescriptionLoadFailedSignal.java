package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;

import java.util.List;

@Value
public class DependsDescriptionLoadFailedSignal implements InstallerSignal
{
    List<String> descriptionLoadFailedDependencies;
}
