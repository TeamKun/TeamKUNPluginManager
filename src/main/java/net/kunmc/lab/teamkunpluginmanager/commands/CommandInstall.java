package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.install.Installer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandInstall
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "エラー： 引数が不足しています！");
            sender.sendMessage(ChatColor.RED + "使用法： /kpm i <Repo|url|name>");
            return;
        }


        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Installer.install(sender, args[0], false);
            }
        }.runTaskAsynchronously(TeamKunPluginManager.plugin);
    }
}
