package org.kunlab.kpm.task.tasks.uninstall.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

/**
 * プラグインのアンロードの前後にスローされるシグナルです。
 */
public class PluginUnloadingSignal extends Signal
{
    /**
     * アンロードの前にスローされるシグナルです。
     */
    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Pre extends PluginUnloadingSignal
    {
        /**
         * アンロードされるプラグインです。
         */
        @NotNull
        Plugin plugin;
    }

    /**
     * アンロードの後にスローされるシグナルです。
     */
    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class Post extends PluginUnloadingSignal
    {
        /**
         * アンロードされるプラグイン<b>の名前</b>です。
         * 注意：アンロード後なので、{@link Plugin}のインスタンスは取得できません(VMから削除されています)。
         */
        @NotNull
        String pluginName;
    }
}
