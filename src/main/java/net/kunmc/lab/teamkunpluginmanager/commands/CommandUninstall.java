package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.Installer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandUninstall
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("kpm.uninstall"))
        {
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");
            return;
        }


        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "E: 引数が不足しています！");
            sender.sendMessage(ChatColor.RED + "使用法: /kpm rm <name>");
            return;
        }

        if (!TeamKunPluginManager.session.lock())
        {
            sender.sendMessage(ChatColor.RED + "E: TeamKunPluginManagerが多重起動しています。");
            return;
        }

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Installer.unInstall(sender, args[0], false);
                TeamKunPluginManager.session.unlock();
            }
        }.runTaskAsynchronously(TeamKunPluginManager.plugin);
    }
}
