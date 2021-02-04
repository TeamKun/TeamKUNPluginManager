package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree.Info.Depend;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandExport
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("kpm.export"))
        {
            sender.sendMessage(ChatColor.RED + "E：権限がありません！");
            return;
        }


        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "エラー： 引数が不足しています！");
            sender.sendMessage(ChatColor.RED + "使用法： /kpm export <all|プラグイン名> [プラグイン名2]...");
            return;
        }

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "情報を変換中...");

        args = Arrays.stream(args).parallel().map(String::toLowerCase).toArray(String[]::new);

        Plugin[] validPlugin;
        if (args[0].equals("all"))
            validPlugin = Bukkit.getPluginManager().getPlugins();
        else
        {
            String[] finalArgs = args;
            validPlugin = Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                    .map(Plugin::getName)
                    .map(String::toLowerCase)
                    .filter(name -> containsIgnoreCase(finalArgs, name))
                    .map(Bukkit.getPluginManager()::getPlugin)
                    .toArray(Plugin[]::new);
        }

        if (validPlugin.length == 0)
        {
            sender.sendMessage(ChatColor.RED + "E: エクスポート可能なプラグインが見つかりませんでした。");
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "この操作で、以下のプラグインがエクスポートされます：");
        sender.sendMessage(ChatColor.GREEN + Arrays.stream(validPlugin).map(Plugin::getName).collect(Collectors.joining(" ")));

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "依存関係ツリーを読み込み中...");

        Plugin[] dependencies = Arrays.stream(validPlugin)
                .flatMap(plugin -> DependencyTree.getInfo(plugin, false).depends.stream())
                .map(depend -> depend.depend.toLowerCase())
                .filter(s -> !containsIgnoreCase(Arrays.stream(validPlugin).map(plugin -> plugin.getName().toLowerCase()).toArray(String[]::new), s))
                .distinct()
                .map(Bukkit.getPluginManager()::getPlugin)
                .toArray(Plugin[]::new);

        sender.sendMessage(ChatColor.GREEN + "また、追加で以下のプラグインがエクスポートされます。");
        sender.sendMessage(ChatColor.GREEN + Arrays.stream(dependencies).map(Plugin::getName).collect(Collectors.joining(" ")));

    }

    public static boolean containsIgnoreCase(String[] target, String maf)
    {
        return Arrays.stream(target).parallel().anyMatch(maf::equalsIgnoreCase);
    }
}
