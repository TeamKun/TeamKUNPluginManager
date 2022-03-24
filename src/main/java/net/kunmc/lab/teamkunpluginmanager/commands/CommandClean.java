package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.Installer;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import net.kunmc.lab.teamkunpluginmanager.utils.Say2Functional;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CommandClean
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("kpm.clean"))
        {
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");
            return;
        }

        if (DependencyTree.isErrors())
        {
            sender.sendMessage(Messages.getErrorMessage());
            sender.sendMessage(ChatColor.RED + "E: エラーが検出されたため、システムが保護されました。");
            return;
        }

        TeamKunPluginManager kpmInstance = TeamKunPluginManager.getPlugin();

        if (!kpmInstance.getSession().lock())
        {
            sender.sendMessage(ChatColor.RED + "E: TeamKunPluginManagerが多重起動しています。");
            return;
        }


        sender.sendMessage(ChatColor.LIGHT_PURPLE + "依存関係ツリーを読み込み中...");

        String[] removable = Installer.getRemovableDataDirs();
        if (removable.length == 0)
        {
            sender.sendMessage(ChatColor.RED + "E: 削除可能が項目が見つかりませんでした。");
            kpmInstance.getSession().unlock();
            return;
        }

        switch (args.length)
        {
            case 2:
            case 1:
                sender.sendMessage(ChatColor.GREEN + "データを検索中...");
                if (!args[0].equals("all") && Arrays.stream(removable).noneMatch(s -> args[0].equalsIgnoreCase(s)))
                {
                    sender.sendMessage(ChatColor.RED + "E: プラグインが見つかりませんでした。");
                    kpmInstance.getSession().unlock();
                    return;
                }

                if (args.length == 2 && !args[1].equals("no-preserve") && !(sender instanceof Player))
                {
                    sender.sendMessage(ChatColor.RED + "本当に実行する場合、次のコマンドを実行してください: /kpm clean " + args[0] + " no-preserve");
                    kpmInstance.getSession().unlock();
                    return;
                }

                if (args.length == 2 && args[1].equals("no-preserve"))
                {
                    sender.sendMessage(ChatColor.GREEN + "削除を実行しています...");
                    if (args[0].equals("all"))
                        Arrays.stream(removable)
                                .forEach(Installer::clean);
                    else
                        Installer.clean(args[0]);
                    sender.sendMessage(Messages.getStatusMessage(0, args[0].equals("all") ? removable.length: 1, 0));
                    sender.sendMessage(ChatColor.GREEN + "S: 削除に成功しました。");
                    kpmInstance.getSession().unlock();
                    return;
                }


                sender.sendMessage(ChatColor.RED + "本当に続行しますか? " +
                        ChatColor.WHITE + "[" +
                        ChatColor.GREEN + "y" +
                        ChatColor.WHITE + "/" +
                        ChatColor.RED + "N" +
                        ChatColor.WHITE + "]");

                kpmInstance.getFunctional().add(
                        ((Player) sender).getUniqueId(),
                        new Say2Functional.FunctionalEntry(StringUtils::startsWithIgnoreCase, s -> {
                            switch (s)
                            {
                                case "n":
                                    sender.sendMessage(ChatColor.RED + "キャンセルしました。");
                                    break;
                                case "y":
                                    sender.sendMessage(ChatColor.GREEN + "削除を実行しています...");
                                    sender.sendMessage(Messages.getStatusMessage(0, args[0].equals("all") ? removable.length: 1, 0));
                                    sender.sendMessage(Messages.getStatusMessage(0, removable.length, 0));
                                    sender.sendMessage(ChatColor.GREEN + "S: 削除に成功しました。");
                            }
                        }, "y", "n")
                );

                break;
            case 0:
                sender.sendMessage(ChatColor.GREEN + "この操作で、以下の" + removable.length + "つのプラグインデータが削除されます: ");
                sender.sendMessage(ChatColor.AQUA + String.join(", ", removable));
                if (!(sender instanceof Player))
                {
                    sender.sendMessage(ChatColor.RED + "本当に実行する場合、次のコマンドを実行してください: /kpm clean all no-preserve");
                    kpmInstance.getSession().unlock();
                    return;
                }
                sender.sendMessage(ChatColor.RED + "本当に続行しますか? " +
                        ChatColor.WHITE + "[" +
                        ChatColor.GREEN + "y" +
                        ChatColor.WHITE + "/" +
                        ChatColor.RED + "N" +
                        ChatColor.WHITE + "]");
                kpmInstance.getFunctional().add(
                        ((Player) sender).getUniqueId(),
                        new Say2Functional.FunctionalEntry(StringUtils::startsWithIgnoreCase, s -> {
                            switch (s)
                            {
                                case "n":
                                    sender.sendMessage(ChatColor.RED + "キャンセルしました。");
                                    kpmInstance.getSession().unlock();
                                    break;
                                case "y":
                                    sender.sendMessage(ChatColor.GREEN + "削除を実行しています...");
                                    Arrays.stream(removable)
                                            .forEach(Installer::clean);
                                    sender.sendMessage(Messages.getStatusMessage(0, removable.length, 0));
                                    sender.sendMessage(ChatColor.GREEN + "S: 削除に成功しました。");
                                    kpmInstance.getSession().unlock();
                            }
                        }, "y", "n")
                );

        }
    }

}
