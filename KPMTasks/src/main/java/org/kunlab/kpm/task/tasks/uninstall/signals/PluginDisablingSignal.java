package org.kunlab.kpm.task.tasks.uninstall.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

/**
 * プラグインの無効化の前後にスローされるシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PluginDisablingSignal extends Signal
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
