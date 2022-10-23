package net.kunmc.lab.kpm.installer.impls.uninstall.signals;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.kpm.signal.Signal;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * プラグインの列挙が完了したことを通知するシグナルです。
 */
@Getter
public class UninstallReadySignal implements Signal
{
    /**
     * アンインストール対象のプラグインのリスト
     */
    private final List<Plugin> plugins;
    /**
     * アンインストールを続行するかどうか
     */
    @Setter
    private boolean continueUninstall;

    public UninstallReadySignal(List<Plugin> plugins)
    {
        this.plugins = plugins;
        this.continueUninstall = true;
    }
}
