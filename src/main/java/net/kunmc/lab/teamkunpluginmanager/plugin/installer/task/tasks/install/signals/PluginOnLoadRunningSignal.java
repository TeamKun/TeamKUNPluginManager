package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@Data
public class PluginOnLoadRunningSignal implements InstallerSignal
{
    @NotNull
    private final Plugin plugin;

    public static class Pre extends PluginOnLoadRunningSignal
    {
        public Pre(@NotNull Plugin plugin)
        {
            super(plugin);
        }
    }

    public static class Post extends PluginOnLoadRunningSignal
    {
        public Post(@NotNull Plugin plugin)
        {
            super(plugin);
        }
    }
}
