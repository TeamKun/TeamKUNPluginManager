package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import lombok.AllArgsConstructor;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.resolver.PluginResolver;

@AllArgsConstructor
class PlumbingInstaller
{
    private static final PluginResolver PLUGIN_RESOLVER;

    static
    {
        PLUGIN_RESOLVER = TeamKunPluginManager.getPlugin().getResolver();
    }

    private final Installer installer;
    private final InstallerSignalHandler signalHandler;

    public InstallProgress initInstall()
    {
        return new InstallProgress();
    }
}
