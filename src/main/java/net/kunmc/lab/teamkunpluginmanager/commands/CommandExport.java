package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.utils.Say2Functional;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class CommandExport
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("kpm.export"))
        {
            sender.sendMessage(ChatColor.RED + "E：権限がありません！");
            return;
        }

        UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId(): null;

        String pluginQuery = null;

        if (args.length > 0)
            pluginQuery = args[0];

        if (pluginQuery == null)
        {
            getStartedAndGetQuery(sender, uuid);
            return;
        }

    }

    private static void getStartedAndGetQuery(CommandSender sender, UUID uuid)
    {
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "プラグインエクスポートウィザードへようこそ！");
        sender.sendMessage(ChatColor.GREEN + "エクスポートしたいプラグインの名前を入力してください。(依存関係は自動でエクスポートされます。)");
        sender.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "Tips: 正規表現を使用可能です。");
        TeamKunPluginManager.functional.add(uuid, new Say2Functional.FunctionalEntry(StringUtils::contains, s -> {
            onCommand(sender, new String[]{s});
        }, ""));
    }

}
