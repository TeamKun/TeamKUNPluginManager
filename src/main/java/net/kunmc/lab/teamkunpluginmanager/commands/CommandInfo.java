package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree.Info;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandInfo
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "エラー： 引数が不足しています！");
            sender.sendMessage(ChatColor.RED + "使用法： /kpm info <プラグイン名>");
        }

        if (Bukkit.getPluginManager().getPlugin(args[0]) == null)
        {
            sender.sendMessage(ChatColor.RED + "E: プラグインが見つかりませんでした。");
            return;
        }

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "依存関係ツリーを読み込み中...");

        Info info = DependencyTree.getInfo(args[0], false);

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "情報を読み込み中...");
        JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(args[0]);

        if (plugin == null)
        {
            sender.sendMessage(ChatColor.RED + "E: 情報の読み込みに失敗しました。");
            return;
        }

        File file = PluginUtil.getFile(plugin);

        sender.sendMessage(pi("名前", info.name));
        sender.sendMessage(pi("作成者", String.join(", ",plugin.getDescription().getAuthors())));
        sender.sendMessage(pi("状態", plugin.isEnabled()));
        sender.sendMessage(pi("読み込みタイミング", PluginUtil.loadToString(plugin.getDescription().getLoad())));
        if (plugin.getDescription().getWebsite() != null)
            sender.sendMessage(pi("ウェブサイト： ", ChatColor.UNDERLINE + plugin.getDescription().getWebsite()));
        if (plugin.getDescription().getPrefix() != null)
            sender.sendMessage(pi("ログ接頭辞", plugin.getDescription().getPrefix()));
        if (plugin.getDescription().getDescription() != null)
            sender.sendMessage(pi("概要", plugin.getDescription().getDescription()));
        if (file != null)
        {
            sender.sendMessage();
            sender.sendMessage(pi("ファイル名", file.getName()));
            sender.sendMessage(pi("ダウンロードサイズ", PluginUtil.getFileSizeString(file.length())));
        }
        sender.sendMessage();
        sender.sendMessage(commandList(plugin.getDescription().getCommands()));

    }

    @SuppressWarnings("unchecked")
    private static BaseComponent[] commandHover(String name, Map<String,Object> command)
    {
        ComponentBuilder builder = new ComponentBuilder(pi("コマンド名", name + "\n\n"));

        if (command.containsKey("aliases"))
            builder.append(pi("エイリアス", "/" + String.join(", /", (List<String>) command.get("aliases"))) + "\n");
        if (command.containsKey("usage"))
            builder.append(pi("使用法", command.get("usage")) + "\n");
        if (command.containsKey("description"))
            builder.append(pi("概要", command.get("description")) + "\n");
        if (command.containsKey("permission"))
            builder.append(pi("権限", command.get("permission")));
        return builder.create();
    }

    private static BaseComponent[] commandList(Map<String, Map<String, Object>> command)
    {
        ComponentBuilder builder = new ComponentBuilder(ChatColor.GREEN + "コマンド： ");

        command.forEach((s, obj) -> {

            ComponentBuilder b = new ComponentBuilder(ChatColor.DARK_GREEN + s);
            b.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, commandHover(s, obj)));
            builder.append(b.create());
        });

        return builder.create();
    }

    private static String pi(String property, String value)
    {
        return ChatColor.GREEN + property + ChatColor.WHITE + "： " +  ChatColor.DARK_GREEN + value;
    }

    private static String pi(String property, boolean a)
    {
        return ChatColor.GREEN + property + ChatColor.WHITE + "： " +  (a ? ChatColor.DARK_GREEN + "はい": ChatColor.RED + "いいえ");
    }

    private static String pi(String property, Object obj)
    {
        return ChatColor.GREEN + property + ChatColor.WHITE + "： " + obj.toString();
    }
}
