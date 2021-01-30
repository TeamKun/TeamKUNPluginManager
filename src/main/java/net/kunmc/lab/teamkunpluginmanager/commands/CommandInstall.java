package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.install.Installer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandInstall
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "エラー：引数が不足しています！");
            sender.sendMessage(ChatColor.RED + "使用法：/kpm i <Repo|url|name>");
        }

        Installer.installFromURLAsync(sender, args[0]);
    }
}
