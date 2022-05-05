package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.assertion;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

@Data
public class IgnoredPluginSignal implements InstallerSignal
{
    @NotNull
    private final String pluginName;
    @NotNull
    private final PluginDescriptionFile pluginDescription;

    private boolean cancelInstall;

    public IgnoredPluginSignal(@NotNull PluginDescriptionFile pluginDescription)
    {
        this.pluginDescription = pluginDescription;
        this.pluginName = pluginDescription.getName();

        this.cancelInstall = true;
    }
}
