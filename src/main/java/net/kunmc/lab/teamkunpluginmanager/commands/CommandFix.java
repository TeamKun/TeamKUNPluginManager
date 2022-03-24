package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
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

        if (!TeamKunPluginManager.getPlugin().getSession().lock())
        {
            sender.sendMessage(ChatColor.RED + "E: TeamKunPluginManagerが多重起動しています。");
            return;
        }

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "依存関係ツリーを読み込み中...");
        if (!DependencyTree.isErrors())
        {
            sender.sendMessage(ChatColor.RED + "E: エラーは検出されませんでした。");
            TeamKunPluginManager.getPlugin().getSession().unlock();
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "エラーを解決しています...");
        DependencyTree.fix();
        sender.sendMessage(ChatColor.GREEN + "S: エラーの解決に成功しました。");
        TeamKunPluginManager.getPlugin().getSession().unlock();
    }
}
