package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskArgument;
import org.bukkit.plugin.Plugin;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = false)
public class UnInstallArgument extends TaskArgument
{
    List<Plugin> plugins;
}
