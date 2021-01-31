package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import org.bukkit.command.CommandSender;

public class CommandStatus
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        sender.sendMessage(Messages.getUnInstallableMessage());
    }
}
