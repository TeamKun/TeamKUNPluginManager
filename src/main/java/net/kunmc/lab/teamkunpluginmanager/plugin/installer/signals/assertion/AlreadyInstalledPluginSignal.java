package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.assertion;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

@Data
public class AlreadyInstalledPluginSignal implements InstallerSignal
{
    @NotNull
    private final PluginDescriptionFile installedPlugin;

    @NotNull
    private final PluginDescriptionFile installingPlugin;

    private boolean replacePlugin;

    public AlreadyInstalledPluginSignal(@NotNull PluginDescriptionFile installedPlugin, @NotNull PluginDescriptionFile installingPlugin)
    {
        this.installedPlugin = installedPlugin;
        this.installingPlugin = installingPlugin;
        this.replacePlugin = false;
    }
}
