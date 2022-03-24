package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandReload
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("kpm.reload"))
        {
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");
            return;
        }

        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "E: 引数が不足しています！");
            sender.sendMessage(ChatColor.RED + "使用法: /kpm reload <Plugin>");
            return;
        }

        Plugin plugin;

        if ((plugin = Bukkit.getPluginManager().getPlugin(args[0])) == null)
        {
            sender.sendMessage(ChatColor.RED + "E: プラグイン " + args[0] + " は存在しません。");
            return;
        }

        if (!TeamKunPluginManager.getPlugin().getSession().lock())
        {
            sender.sendMessage(ChatColor.RED + "E: TeamKunPluginManagerが多重起動しています。");
            return;
        }

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                PluginUtil.reload(plugin);
                sender.sendMessage(ChatColor.GREEN + "S: " + args[0] + " を正常に再読み込みしました。");
                TeamKunPluginManager.getPlugin().getSession().unlock();
            }
        }.runTaskAsynchronously(TeamKunPluginManager.getPlugin());
    }
}
