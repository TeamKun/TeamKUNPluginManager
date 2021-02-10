package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.IOException;

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

        TeamKunPluginManager.config.set("oauth", args[0]);

        try
        {
            TeamKunPluginManager.config.save("config.yml");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "E: 設定の保存に失敗しました！");
        }
    }
}
