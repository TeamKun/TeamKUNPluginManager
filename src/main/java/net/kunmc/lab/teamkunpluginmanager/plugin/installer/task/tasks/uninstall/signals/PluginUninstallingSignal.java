package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.Signal;
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

