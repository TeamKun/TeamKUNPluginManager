package org.kunlab.kpm.installer.impls.install.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

/**
 * プラグインが既にインストールされていることを示すシグナルです。
 * {@link AlreadyInstalledPluginSignal#setReplacePlugin(boolean)} を用いて、既存のプラグインを新規プラグインに置換するかどうかを設定できます。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AlreadyInstalledPluginSignal extends Signal
{
    /**
     * インストールされたプラグインの種類
     */
    @NotNull
    private final PluginDescriptionFile installedPlugin;

    @NotNull
    private final PluginDescriptionFile installingPlugin;

    private boolean replacePlugin;

    /**
     * コンストラクタです。
     *
     * @param installedPlugin  既にインストールされているプラグインの {@link PluginDescriptionFile}
     * @param installingPlugin インストールしようとしているプラグインの {@link PluginDescriptionFile}
     */
    public AlreadyInstalledPluginSignal(@NotNull PluginDescriptionFile installedPlugin, @NotNull PluginDescriptionFile installingPlugin)
    {
        this.installedPlugin = installedPlugin;
        this.installingPlugin = installingPlugin;
        this.replacePlugin = false;
    }
}
