package net.kunmc.lab.kpm.installer.impls.uninstall.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.signal.Signal;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * アンインストールしようとしたプラグインが、他のプラグインの依存関係にあることを示すシグナルです。
 * {@link PluginIsDependencySignal#setForceUninstall(boolean)} (boolean)} を用いて、強制的にアンインストールするかどうかを設定できます。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PluginIsDependencySignal extends Signal
{
    @NotNull
    private final Plugin plugin;
    @NotNull
    private final List<Plugin> dependedBy;

    /**
     * 強制的にアンインストールするかどうかを示すフラグです。
     * このフラグが {@code true} の場合、このプラグインの依存関係にあるプラグインも一緒にアンインストールされます。
     */
    private boolean forceUninstall;

    public PluginIsDependencySignal(@NotNull Plugin plugin, @NotNull List<Plugin> dependedBy)
    {
        this.plugin = plugin;
        this.dependedBy = dependedBy;
        this.forceUninstall = false;
    }
}
