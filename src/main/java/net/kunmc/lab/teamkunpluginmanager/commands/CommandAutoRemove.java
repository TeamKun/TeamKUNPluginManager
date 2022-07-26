package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.Installer;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandAutoRemove extends CommandBase
{
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

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    { // TODO: Separate frontend and backend
        if (checkPermission(sender, terminal, "kpm.autoremove"))
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");

        ArrayList<String> rem = new ArrayList<>();

        if (!TeamKunPluginManager.getPlugin().getSession().lock())
        {
            sender.sendMessage(ChatColor.RED + "E: TeamKunPluginManagerが多重起動しています。");
            return;
        }

        AtomicInteger removed = new AtomicInteger();
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "依存関係ツリーを読み込み中...");

        ArrayList<String> removables = DependencyTree.unusedPlugins();
        if (removables.isEmpty())
        {
            sender.sendMessage(ChatColor.RED + "E: 削除可能なプラグインはありません。");
            sender.sendMessage(Messages.getStatusMessage(0, removed.get(), 0));
            sender.sendMessage(ChatColor.GREEN + "S: 操作が正常に完了しました。");
            TeamKunPluginManager.getPlugin().getSession().unlock();
        }

        while (!removables.isEmpty())
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
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.autoremove";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("必要がなくなったプラグインを自動で削除します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
    }
}
