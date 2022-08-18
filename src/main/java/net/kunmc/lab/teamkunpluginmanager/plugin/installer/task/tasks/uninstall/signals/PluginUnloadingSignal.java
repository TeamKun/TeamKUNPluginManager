package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PluginUnloadingSignal implements InstallerSignal
{
    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Pre extends PluginUnloadingSignal
    {
        @NotNull
        Plugin plugin;
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Post extends PluginUnloadingSignal
    {
        @NotNull
        String pluginName;
    }
}
