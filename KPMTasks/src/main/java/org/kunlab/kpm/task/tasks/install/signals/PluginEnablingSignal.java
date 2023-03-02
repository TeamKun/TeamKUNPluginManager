package org.kunlab.kpm.task.tasks.install.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

/**
 * プラグインの有効化中であることを示すシグナルです。
 * {@link org.bukkit.plugin.java.JavaPlugin#onEnable()} の呼び出しの前後にスローされます。
 * このシグナルより先に {@link PluginOnLoadRunningSignal} が呼ばれます。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PluginEnablingSignal extends Signal
{
    /**
     * 対象のプラグインです。
     */
    @NotNull
    private final Plugin plugin;

    /**
     * 有効化を行う前に送信されるシグナルです。
     */
    public static class Pre extends PluginEnablingSignal
    {
        public Pre(@NotNull Plugin plugin)
        {
            super(plugin);
        }
    }

    /**
     * 有効化中に失敗した場合に送信されるシグナルです。
     */
    public static class Failed extends PluginEnablingSignal
    {
        public Failed(@NotNull Plugin plugin)
        {
            super(plugin);
        }
    }

    /**
     * 有効化を行った後に送信されるシグナルです。
     */
    public static class Post extends PluginEnablingSignal
    {
        public Post(@NotNull Plugin plugin)
        {
            super(plugin);
        }
    }
}
