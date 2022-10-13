package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.uninstall.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * プラグインの無効化の前後にスローされるシグナルです。
 */
@Data
public class PluginDisablingSignal implements Signal
{
    /**
     * 無効化されるプラグインです。
     */
    @NotNull
    private final Plugin plugin;

    /**
     * 無効化の前にスローされるシグナルです。
     */
    public static class Pre extends PluginDisablingSignal
    {
        public Pre(@NotNull Plugin plugin)
        {
            super(plugin);
        }
    }

    /**
     * 無効化の後にスローされるシグナルです。
     */
    public static class Post extends PluginDisablingSignal
    {
        public Post(@NotNull Plugin plugin)
        {
            super(plugin);
        }
    }
}
