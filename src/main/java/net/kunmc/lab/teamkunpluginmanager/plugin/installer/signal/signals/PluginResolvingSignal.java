package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.resolver.PluginResolver;

@AllArgsConstructor
@Data
public class PluginResolvingSignal implements InstallerSignal
{
    private String query;
    private final PluginResolver resolver;
}
