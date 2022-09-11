package net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.Signal;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * アンインストールしようとしたプラグインが、他のプラグインの依存関係にあることを示すシグナルです。
 * {@link PluginIsDependencySignal#setForceUninstall(boolean)} (boolean)} を用いて、強制的にアンインストールするかどうかを設定できます。
 */
@Data
public class PluginIsDependencySignal implements Signal
{
    @NotNull
    private final String pluginName;
    @NotNull
    private final List<Plugin> dependedBy;

    /**
     * 強制的にアンインストールするかどうかを示すフラグです。
     * このフラグが {@code true} の場合、このプラグインの依存関係にあるプラグインも一緒にアンインストールされます。
     */
    private boolean forceUninstall;

    public PluginIsDependencySignal(@NotNull String pluginName, @NotNull List<Plugin> dependedBy)
    {
        this.pluginName = pluginName;
        this.dependedBy = dependedBy;
        this.forceUninstall = false;
    }
}
