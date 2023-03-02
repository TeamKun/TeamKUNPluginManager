package org.kunlab.kpm.installer.impls.uninstall.signals;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.Plugin;
import org.kunlab.kpm.signal.Signal;

import java.util.List;

/**
 * プラグインの列挙が完了したことを通知するシグナルです。
 */
@Getter
public class UninstallReadySignal extends Signal
{
    /**
     * アンインストール対象のプラグインのリストです。
     */
    private final List<Plugin> plugins;
    /**
     * 自動でアンインストールを行うかどうかのフラグです。
     */
    private final boolean autoConfirm;
    /**
     * アンインストールを続行するかどうかです。
     */
    @Setter
    private boolean continueUninstall;

    public UninstallReadySignal(List<Plugin> plugins, boolean autoConfirm)
    {
        this.plugins = plugins;
        this.autoConfirm = autoConfirm;
        this.continueUninstall = true;
    }
}
