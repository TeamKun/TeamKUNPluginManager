package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.install.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * プラグインの {@link Plugin#onLoad()} の実行の前後にスローされるシグナルです。
 */
@Data
public class PluginOnLoadRunningSignal implements Signal
{
    /**
     * 対象のプラグインです。
     */
    @NotNull
    private final Plugin plugin;

    /**
     * {@link Plugin#onLoad()} の実行を行う前に送信されるシグナルです。
     */
    public static class Pre extends PluginOnLoadRunningSignal
    {
        public Pre(@NotNull Plugin plugin)
        {
            super(plugin);
        }
    }

    /**
     * {@link Plugin#onLoad()} の実行を行った後に送信されるシグナルです。
     */
    public static class Post extends PluginOnLoadRunningSignal
    {
        public Post(@NotNull Plugin plugin)
        {
            super(plugin);
        }
    }
}
