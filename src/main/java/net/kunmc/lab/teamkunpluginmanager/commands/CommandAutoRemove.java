package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.Installer;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandAutoRemove
{
    public static ArrayList<String> onCommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("kpm.autoremove"))
        {
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");
            return null;
        }

        ArrayList<String> rem = new ArrayList<>();

        if (!TeamKunPluginManager.getPlugin().getSession().lock())
        {
            sender.sendMessage(ChatColor.RED + "E: TeamKunPluginManagerが多重起動しています。");
            return null;
        }

        AtomicInteger removed = new AtomicInteger();
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "依存関係ツリーを読み込み中...");

        ArrayList<String> removables = DependencyTree.unusedPlugins();
        if (removables.size() == 0)
        {
            sender.sendMessage(ChatColor.RED + "E: 削除可能なプラグインはありません。");
            sender.sendMessage(Messages.getStatusMessage(0, removed.get(), 0));
            sender.sendMessage(ChatColor.GREEN + "S: 操作が正常に完了しました。");
            TeamKunPluginManager.getPlugin().getSession().unlock();
            return rem;
        }

        while (removables.size() > 0)
        { // のremoveUnusedPluginsで削除したプラグインによりいらない物ができた場合のためのループ
            rem.addAll(removeUnusedPlugins(removables));
            removables = DependencyTree.unusedPlugins();
        }

        rem.forEach(s -> {
            sender.sendMessage(Messages.getModifyMessage(Messages.ModifyType.REMOVE, s));
            removed.getAndIncrement();
        });
        sender.sendMessage(Messages.getStatusMessage(0, removed.get(), 0));

        TeamKunPluginManager.getPlugin().getSession().unlock();
        return rem;
    }

    private static List<String> removeUnusedPlugins(ArrayList<String> removables)
    {
        ArrayList<String> removed = new ArrayList<>();

        for (String removable : removables)
        {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(removable);
            if (plugin != null)
            {
                Installer.unInstall(null, removable, true);
                removed.add(removable);
            }
            DependencyTree.purge(removable);
        }

        return removed;
    }
}
