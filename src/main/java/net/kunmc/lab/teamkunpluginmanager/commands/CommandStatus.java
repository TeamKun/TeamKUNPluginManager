package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommandStatus
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("kpm.status"))
        {
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");
            return;
        }


        sender.sendMessage(ChatColor.GREEN + "===現在の状態===");

        String statusError = Messages.getErrorMessage();

        sender.sendMessage(ChatColor.GREEN + "ステータス: " + (!statusError.equals("") ? ChatColor.RED + "エラー": ChatColor.DARK_GREEN + "正常"));
        sender.sendMessage(pi("プラグイン数", Bukkit.getPluginManager().getPlugins().length));

        File resolve = new File(
                TeamKunPluginManager.getPlugin().getDataFolder(),
                TeamKunPluginManager.getPlugin().getConfig().getString("resolvePath")
        );
        if (resolve.exists())
            sender.sendMessage(pi("最終アップデート", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(resolve.lastModified()))));
        if (!statusError.equals(""))
            sender.sendMessage(statusError);

        String autoRemovable = Messages.getUnInstallableMessage();

        if (!autoRemovable.equals(""))
            sender.sendMessage(autoRemovable);

        if (!TeamKunPluginManager.getPlugin().isTokenAvailable())
            sender.sendMessage(ChatColor.RED + "トークンがセットされていません！/kpm register でトークンを発行してください。");
    }

    private static String pi(String property, String value)
    {
        return ChatColor.GREEN + property + ChatColor.WHITE + ": " + ChatColor.DARK_GREEN + value;
    }

    private static String pi(String property, boolean a)
    {
        return ChatColor.GREEN + property + ChatColor.WHITE + ": " + (a ? ChatColor.DARK_GREEN + "はい": ChatColor.RED + "いいえ");
    }

    private static String pi(String property, Object obj)
    {
        return ChatColor.GREEN + property + ChatColor.WHITE + ": " + ChatColor.GREEN + obj.toString();
    }
}
