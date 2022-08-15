package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve.signals;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.resolver.PluginResolver;

@Data
@AllArgsConstructor
public class PluginResolvingSignal implements InstallerSignal
{
    private String query;
    private final PluginResolver resolver;
}
