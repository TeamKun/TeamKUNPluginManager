package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandRegister
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("kpm.register"))
        {
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");
            return;
        }

        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "E: 引数が不足しています！");
            sender.sendMessage(ChatColor.RED + "使用法: /kpm register <Token>");
            return;
        }


        TeamKunPluginManager.vault.vault(args[0]);
        sender.sendMessage(ChatColor.GREEN + "S: トークンを正常に保管しました！");

    }
}
