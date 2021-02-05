package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.compactor.PluginCompacter;
import net.kunmc.lab.teamkunpluginmanager.plugin.compactor.PluginPreCompacter;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandExport
{
    public static HashMap<UUID, PluginPreCompacter> session = new HashMap<>();

    public static void onCommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("kpm.export"))
        {
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");
            return;
        }

        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "E: 引数が不足しています！");
            sender.sendMessage(ChatColor.RED + "使用法: /kpm export <all|プラグイン名> [プラグイン名2]...");
            return;
        }

        if (DependencyTree.isErrors())
        {
            sender.sendMessage(Messages.getErrorMessage());
            sender.sendMessage(ChatColor.RED + "E: エラーが検出されたため、システムが保護されました。");
            return;
        }

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "情報を変換中...");

        args = Arrays.stream(args).parallel().map(String::toLowerCase).toArray(String[]::new);

        String[] validPlugin;
        if (args[0].equals("all"))
            validPlugin = Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).toArray(String[]::new);
        else
        {
            String[] finalArgs = args;
            validPlugin = Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                    .map(Plugin::getName)
                    .map(String::toLowerCase)
                    .filter(name -> containsIgnoreCase(finalArgs, name))
                    .toArray(String[]::new);
        }

        if (validPlugin.length == 0)
        {
            sender.sendMessage(ChatColor.RED + "E: エクスポート可能なプラグインが見つかりませんでした。");
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "この操作で、以下のプラグインがエクスポートされます: ");
        sender.sendMessage(ChatColor.GREEN + Arrays.stream(validPlugin).map(s -> Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(s)).getName()).collect(Collectors.joining(" ")));

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "依存関係ツリーを読み込み中...");

        String[] finalValidPlugin = validPlugin;
        String[] dependencies = Arrays.stream(validPlugin)
                .flatMap(plugin -> DependencyTree.getInfo(plugin, false).depends.stream())
                .map(depend -> depend.depend.toLowerCase())
                .filter(s -> !containsIgnoreCase(Arrays.stream(finalValidPlugin).map(String::toLowerCase).toArray(String[]::new), s))
                .distinct()
                .toArray(String[]::new);

        if (dependencies.length != 0)
        {
            sender.sendMessage(ChatColor.GREEN + "また、追加で以下のプラグインがエクスポートされます。");
            sender.sendMessage(ChatColor.GREEN + Arrays.stream(dependencies).map(s -> Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(s)).getName()).collect(Collectors.joining(" ")));
        }

        validPlugin = (String[]) ArrayUtils.addAll(validPlugin, dependencies);

        sender.sendMessage("プラグインをバンドル中...");

        PluginPreCompacter compacter = new PluginPreCompacter();
        compacter.addAll(validPlugin);
        session.put(sender instanceof Player ? ((Player)sender).getUniqueId(): null, compacter);
        fixError(sender instanceof Player ? ((Player)sender).getUniqueId(): null);
        //fixErrorに引き継ぎする
    }

    public static void fixError(UUID uuid)
    {

    }

    public static boolean containsIgnoreCase(String[] target, String maf)
    {
        return Arrays.stream(target).parallel().anyMatch(maf::equalsIgnoreCase);
    }
}
