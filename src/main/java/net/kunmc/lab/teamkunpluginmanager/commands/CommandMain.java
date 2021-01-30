package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandMain implements CommandExecutor
{
    @Override
    public boolean onCommand( CommandSender sender,  Command command,  String label,  String[] args)
    {
        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "エラー：不明なコマンドです！");
            sender.sendMessage(Messages.getCommandNotFoundMessage());
            return true;
        }

        if (!sender.hasPermission("kpm.use"))
        {
            sender.sendMessage(ChatColor.RED + "エラー：権限がありません！");
            return true;
        }

        ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));

        argsList.remove(0);

        switch (args[0].toLowerCase())
        {
            case "install":
            case "i":
                CommandInstall.onCommand(sender, argsList.toArray(new String[0]));
                break;
            default:
                sender.sendMessage(ChatColor.RED + "エラー：不明なコマンドです！");
                sender.sendMessage(Messages.getCommandNotFoundMessage());
                break;
        }

        return true;
    }


}
