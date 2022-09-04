package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * プラグインのアンロードの前後にスローされるシグナルです。
 */
public class PluginUnloadingSignal implements InstallerSignal
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
