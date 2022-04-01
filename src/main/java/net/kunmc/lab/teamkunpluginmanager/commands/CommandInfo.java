package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree.Info;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandInfo
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("kpm.info"))
        {
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");
            return;
        }


        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "E: 引数が不足しています！");
            sender.sendMessage(ChatColor.RED + "使用法: /kpm info <プラグイン名>");
            return;
        }

        if (!PluginUtil.isPluginLoaded(args[0]))
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
        sender.sendMessage(pi("作成者", String.join(", ", plugin.getDescription().getAuthors())));
        sender.sendMessage(pi("状態", plugin.isEnabled() ? ChatColor.DARK_GREEN + "有効": ChatColor.RED + "無効"));
        sender.sendMessage(pi("読み込みタイミング", PluginUtil.loadToString(plugin.getDescription().getLoad())));
        sender.sendMessage(pi("保護", TeamKunPluginManager.getPlugin().getPluginConfig().getStringList("ignore").stream().anyMatch(s -> s.equalsIgnoreCase(info.name))));

        if (plugin.getDescription().getWebsite() != null)
            sender.sendMessage(pi("ウェブサイト", ChatColor.UNDERLINE + plugin.getDescription().getWebsite()));
        if (plugin.getDescription().getPrefix() != null)
            sender.sendMessage(pi("ログ接頭辞", plugin.getDescription().getPrefix()));
        if (plugin.getDescription().getDescription() != null)
            sender.sendMessage(pi("概要", plugin.getDescription().getDescription()));

        if (file != null)
        {
            sender.sendMessage("");
            sender.sendMessage(pi("ファイル名", file.getName()));
            sender.sendMessage(pi("ダウンロードサイズ", PluginUtil.getFileSizeString(file.length())));
        }

        sender.sendMessage("");
        sender.sendMessage(dependTree("依存関係", plugin.getDescription().getDepend()));
        sender.sendMessage(dependTree("被依存関係", info.rdepends.stream().parallel().map(depend -> depend.depend).collect(Collectors.toList())));

        sender.sendMessage("");
        sender.sendMessage(commandList(plugin.getDescription().getCommands()));

    }

    private static Component dependTree(String name, List<String> l)
    {
        TextComponent content = Component.text(ChatColor.GREEN + name + ": ");

        if (l.size() == 0)
            return content.append(Component.text(ChatColor.DARK_GREEN + "なし")).asComponent();

        for(String depend: l)
        {
            content = content.append(Component.text(" " + ChatColor.DARK_GREEN + depend)
                    .hoverEvent(HoverEvent.showText(Component.text(ChatColor.AQUA + "クリックして詳細を表示")))
                    .clickEvent(ClickEvent.runCommand("/kpm info " + depend)));
        }

        return content;
    }

    @SuppressWarnings("unchecked")
    private static Component commandHover(String name, Map<String, Object> command)
    {
        Component component = Component.text(pi("コマンド名", name + "\n\n"));

        if (command.containsKey("aliases"))
        {
            if (command.get("aliases") instanceof String)
                component = component.append(Component.text(pi("エイリアス", "/" + command.get("aliases"))));
            else if (command.get("aliases") instanceof List)
                component = component.append(Component.text(pi("エイリアス",
                        "/" + String.join(", /", (List<String>) command.get("aliases"))) + "\n"));
        }

        if (command.containsKey("usage"))
            component = component.append(Component.text(pi("使用法", command.get("usage")) + "\n"));
        if (command.containsKey("description"))
            component = component.append(Component.text(pi("概要", command.get("description")) + "\n"));
        if (command.containsKey("permission"))
            component = component.append(Component.text(pi("権限", command.get("permission")) + "\n"));
        return component;
    }

    private static Component commandList(Map<String, Map<String, Object>> command)
    {
        TextComponent component = Component.text(ChatColor.GREEN + "コマンド：");

        for (Map.Entry<String, Map<String, Object>> c: command.entrySet())
        {
            component = component.append(
                    Component.text(ChatColor.DARK_GREEN + " /" + c.getKey())
                    .hoverEvent(HoverEvent.showText(commandHover(c.getKey(), c.getValue()))));
        }

        return component;
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
        return ChatColor.GREEN + property + ChatColor.WHITE + ": " + obj.toString();
    }
}
