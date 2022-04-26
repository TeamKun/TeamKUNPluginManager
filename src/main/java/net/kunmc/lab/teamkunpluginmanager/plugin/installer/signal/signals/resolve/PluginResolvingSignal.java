package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.resolve;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.resolver.PluginResolver;

@Data
public class PluginResolvingSignal implements InstallerSignal
{
    private String query;
    private final PluginResolver resolver;
}
