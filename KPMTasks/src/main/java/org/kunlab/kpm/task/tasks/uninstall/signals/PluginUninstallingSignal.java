package org.kunlab.kpm.task.tasks.uninstall.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.plugin.Plugin;
import org.kunlab.kpm.signal.Signal;

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

