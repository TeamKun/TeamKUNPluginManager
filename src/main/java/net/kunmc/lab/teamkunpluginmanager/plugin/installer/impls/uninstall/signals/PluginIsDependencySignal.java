package net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class PluginIsDependencySignal implements InstallerSignal
{
    @NotNull
    private final String pluginName;
    @NotNull
    private final List<Plugin> dependedBy;

    /**
     * Force uninstall plugin.
     * If this turn to true, it will uninstall the plugin and all its dependencies.
     */
    private boolean forceUninstall;

    public PluginIsDependencySignal(@NotNull String pluginName, @NotNull List<Plugin> dependedBy)
    {
        this.pluginName = pluginName;
        this.dependedBy = dependedBy;
        this.forceUninstall = false;
    }
}
