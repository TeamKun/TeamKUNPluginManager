package net.kunmc.lab.teamkunpluginmanager.spigot.commands;

import net.kunmc.lab.teamkunpluginmanager.common.Variables;
import net.kunmc.lab.teamkunpluginmanager.spigot.plugin.DependencyTree;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandFix
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("kpm.fix"))
        {
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");
            return;
        }

        if (!Variables.session.lock())
        {
            sender.sendMessage(ChatColor.RED + "E: TeamKunPluginManagerが多重起動しています。");
            return;
        }

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "依存関係ツリーを読み込み中...");
        if (!DependencyTree.isErrors())
        {
            sender.sendMessage(ChatColor.RED + "E: エラーは検出されませんでした。");
            Variables.session.unlock();
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "エラーを解決しています...");
        DependencyTree.fix();
        sender.sendMessage(ChatColor.GREEN + "S: エラーの解決に成功しました。");
        Variables.session.unlock();
    }
}
