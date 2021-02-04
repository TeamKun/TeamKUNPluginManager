package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandMain implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!sender.hasPermission("kpm.main"))
        {
            sender.sendMessage(ChatColor.RED + "E：権限がありません！");
            return true;
        }

        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "E：不明なコマンドです！");
            sender.sendMessage(Messages.getCommandNotFoundMessage());
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
            case "uninstall":
            case "remove":
            case "rm":
                CommandUninstall.onCommand(sender, argsList.toArray(new String[0]));
                break;
            case "status":
                CommandStatus.onCommand(sender, argsList.toArray(new String[0]));
                break;
            case "autoremove":
                CommandAutoRemove.onCommand(sender, argsList.toArray(new String[0]));
                break;
            case "fix":
                CommandFix.onCommand(sender, argsList.toArray(new String[0]));
                break;
            case "update":
                CommandUpdate.onCommand(sender, argsList.toArray(new String[0]));
                break;
            case "clean":
                CommandClean.onCommand(sender, argsList.toArray(new String[0]));
                break;
            case "info":
                CommandInfo.onCommand(sender, argsList.toArray(new String[0]));
                break;
            case "export":
                CommandExport.onCommand(sender, argsList.toArray(new String[0]));
                break;
            default:
                sender.sendMessage(ChatColor.RED + "エラー： 不明なコマンドです！");
                sender.sendMessage(Messages.getCommandNotFoundMessage());
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        ArrayList<String> completes = new ArrayList<>();

        if (!sender.hasPermission("kpm.use"))
            return new ArrayList<>();

        switch (args.length)
        {
            case 1:
                completes.addAll(Arrays.asList("install", "i", "uninstall", "remove", "rm", "status", "autoremove", "fix", "update", "clean", "info"));
                break;
            case 2:
                String cmd = args[0];
                switch (cmd)
                {
                    case "uninstall":
                    case "rm":
                    case "remove":
                    case "info":
                    case "clean":
                        completes = Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).collect(Collectors.toCollection(ArrayList::new));

                        break;
                }
            case 3:
                if (args[0].equals("clean"))
                {
                    completes = Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).collect(Collectors.toCollection(ArrayList::new));
                    completes.add("all");
                }
        }

        ArrayList<String> asCopy = new ArrayList<>();
        StringUtil.copyPartialMatches(args[args.length - 1], completes, asCopy);
        Collections.sort(asCopy);
        return asCopy;
    }
}
