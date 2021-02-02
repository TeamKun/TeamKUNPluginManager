package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.install.Installer;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandAutoRemove
{
    public static ArrayList<String> onCommand(CommandSender sender, String[] args)
    {
        if (sender != null && !sender.hasPermission("kpm.autoremove"))
        {
            sender.sendMessage(ChatColor.RED + "E：権限がありません！");
            return null;
        }

        ArrayList<String> rem = new ArrayList<>();
        if (sender == null)
            sender = Installer.dummySender();

        AtomicInteger removed = new AtomicInteger();
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "依存関係ツリーを読み込み中...");
        ArrayList<String> removables = DependencyTree.unusedPlugins();
        if (removables.size() == 0)
        {
            sender.sendMessage(ChatColor.RED + "E: 削除可能なプラグインはありません。");
            sender.sendMessage(Messages.getStatusMessage(0, removed.get(), 0));
            sender.sendMessage(ChatColor.GREEN + "S: 操作が正常に完了しました。");
            return rem;
        }

        for (String removable : removables)
        {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(removable);
            if (plugin != null)
            {
                Installer.unInstall(null, removable);
                rem.add(removable);
            }
            DependencyTree.purge(removable);
        }

        rem.addAll(onCommand(null, null));

        if (!sender.equals(Installer.dummySender()))
        {
            CommandSender finalSender = sender;
            rem.forEach(s -> {
                finalSender.sendMessage(Messages.getModifyMessage(Messages.ModifyType.REMOVE, s));
                removed.getAndIncrement();
            });
            sender.sendMessage(Messages.getStatusMessage(0, removed.get(), 0));
        }
        return rem;
    }
}
