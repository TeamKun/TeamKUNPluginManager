package org.kunlab.kpm.task.tasks.install.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

/**
 * プラグインの {@link Plugin#onLoad()} の実行の前後にスローされるシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PluginOnLoadRunningSignal extends Signal
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
