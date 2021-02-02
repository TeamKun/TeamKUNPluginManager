package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandStatus
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("kpm.status"))
        {
            sender.sendMessage(ChatColor.RED + "E：権限がありません！");
            return;
        }


        sender.sendMessage(ChatColor.GREEN + "===現在の状態===");

        String statusError = Messages.getErrorMessage();

        sender.sendMessage(ChatColor.YELLOW + "ステータス： " + (!statusError.equals("") ? ChatColor.RED + "エラー": ChatColor.GREEN + "正常"));
        if (!statusError.equals(""))
            sender.sendMessage(statusError);

        String autoRemovable = Messages.getUnInstallableMessage();

        if (!autoRemovable.equals(""))
            sender.sendMessage(autoRemovable);
    }
}
