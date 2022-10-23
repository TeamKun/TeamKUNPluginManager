package net.kunmc.lab.kpm.installer.task.tasks.uninstall;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.installer.task.TaskArgument;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * プラグインのアンインストールを行うタスクの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class UnInstallArgument extends TaskArgument
{
    /**
     * アンインストールされるプラグインです。
     */
    List<Plugin> plugins;
}
