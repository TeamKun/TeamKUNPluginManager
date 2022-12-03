package net.kunmc.lab.kpm.installer.task.tasks.uninstall.hard.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.signal.Signal;
import org.bukkit.plugin.Plugin;

/**
 * プラグインのアンインストールのシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class PluginUninstallingSignal extends Signal
{
    /**
     * アンインストールされるプラグインです。
     */
    Plugin plugin;
}

