package net.kunmc.lab.kpm.task.tasks.uninstall.signals;

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

