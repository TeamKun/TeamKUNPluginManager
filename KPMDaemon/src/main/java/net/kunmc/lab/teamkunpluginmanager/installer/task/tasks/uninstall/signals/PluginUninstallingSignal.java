package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.uninstall.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;
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

