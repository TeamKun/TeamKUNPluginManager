package net.kunmc.lab.kpm.installer.impls.install.signals;

import lombok.Data;
import net.kunmc.lab.kpm.signal.Signal;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

/**
 * プラグインが既にインストールされていることを示すシグナルです。
 * {@link AlreadyInstalledPluginSignal#setReplacePlugin(boolean)} を用いて、既存のプラグインを新規プラグインに置換するかどうかを設定できます。
 */
@Data
public class AlreadyInstalledPluginSignal implements Signal
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
