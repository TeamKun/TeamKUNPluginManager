package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.install.Installer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandUninstall
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "エラー：引数が不足しています！");
            sender.sendMessage(ChatColor.RED + "使用法：/kpm rm <name>");
        }

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Installer.unInstall(sender, args[0]);
            }
        }.runTaskAsynchronously(TeamKunPluginManager.plugin);
    }
}
