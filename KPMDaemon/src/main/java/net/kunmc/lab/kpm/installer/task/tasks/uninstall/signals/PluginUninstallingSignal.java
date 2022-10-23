package net.kunmc.lab.kpm.installer.task.tasks.uninstall.signals;

import lombok.Value;
import net.kunmc.lab.kpm.signal.Signal;
import org.bukkit.plugin.Plugin;

/**
 * プラグインのアンインストールのシグナルです。
 */
@Value
public class PluginUninstallingSignal implements Signal
{
    /**
     * アンインストールされるプラグインです。
     */
    Plugin plugin;
}

