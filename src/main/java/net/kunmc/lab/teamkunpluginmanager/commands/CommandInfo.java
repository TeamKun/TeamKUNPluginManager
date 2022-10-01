package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.meta.DependType;
import net.kunmc.lab.teamkunpluginmanager.plugin.meta.PluginMeta;
import net.kunmc.lab.teamkunpluginmanager.plugin.meta.PluginMetaProvider;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import net.kunmc.lab.teamkunpluginmanager.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandInfo extends CommandBase
{
    private static Component dependTree(String name, List<String> l)
    {
        TextComponent content = Component.text(name + ": ");

        if (l.isEmpty())
            return content.append(Component.text(ChatColor.GRAY + "なし")).asComponent();

        for (String depend : l)
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
        Component component = Component.text(Messages.keyValue("コマンド名", name + "\n\n"));

        if (command.containsKey("aliases"))
        {
            if (command.get("aliases") instanceof String)
                component = component.append(Component.text(Messages.keyValue("エイリアス", "/" + command.get("aliases"))));
            else if (command.get("aliases") instanceof List)
                component = component.append(Component.text(Messages.keyValue(
                        "エイリアス",
                        "/" + String.join(", /", (List<String>) command.get("aliases"))
                ) + "\n"));
        }

        if (command.containsKey("usage"))
            component = component.append(Component.text(Messages.keyValue("使用法", command.get("usage")) + "\n"));
        if (command.containsKey("description"))
            component = component.append(Component.text(Messages.keyValue("概要", command.get("description")) + "\n"));
        if (command.containsKey("permission"))
            component = component.append(Component.text(Messages.keyValue("権限", command.get("permission")) + "\n"));
        return component;
    }

    private static Component commandList(Map<String, Map<String, Object>> command)
    {
        TextComponent component = Component.text(ChatColor.GREEN + "コマンド：");

        for (Map.Entry<String, Map<String, Object>> c : command.entrySet())
        {
            component = component.append(
                    Component.text(ChatColor.DARK_GREEN + " /" + c.getKey())
                            .hoverEvent(HoverEvent.showText(commandHover(c.getKey(), c.getValue()))));
        }

        return component;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1, 1))
            return;

        terminal.info("依存関係ツリーを読み込み中...");

        PluginMetaProvider provider = TeamKunPluginManager.getPlugin().getPluginMetaManager().getProvider();

        if (provider.isPluginMetaExists(args[0]))
        {
            terminal.error("プラグインが見つかりませんでした。");
            return;
        }

        PluginMeta meta = provider.getPluginMeta(
                args[0], true
        );

        terminal.info("情報を読み込み中...");
        JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(args[0]);

        if (plugin == null)
        {
            terminal.error("情報の読み込みに失敗しました。");
            return;
        }

        File file = PluginUtil.getFile(plugin);

        terminal.writeLine(Messages.keyValue("名前", meta.getName()));
        terminal.writeLine(Messages.keyValue("作成者", String.join(", ", plugin.getDescription().getAuthors())));
        terminal.writeLine(Messages.keyValue("状態", plugin.isEnabled() ? ChatColor.DARK_GREEN + "有効": ChatColor.RED + "無効"));
        terminal.writeLine(Messages.keyValue("読み込みタイミング", PluginUtil.loadToString(plugin.getDescription().getLoad())));
        terminal.writeLine(Messages.keyValueYesNo(
                "保護",
                TeamKunPluginManager.getPlugin().getPluginConfig().getStringList("ignore").stream().parallel()
                        .anyMatch(s -> s.equalsIgnoreCase(meta.getName()))
        ));

        if (plugin.getDescription().getWebsite() != null)
            terminal.writeLine(Messages.keyValue("ウェブサイト", ChatColor.UNDERLINE + plugin.getDescription().getWebsite()));
        if (plugin.getDescription().getPrefix() != null)
            terminal.writeLine(Messages.keyValue("ログ接頭辞", plugin.getDescription().getPrefix()));
        if (plugin.getDescription().getDescription() != null)
            terminal.writeLine(Messages.keyValue("概要", plugin.getDescription().getDescription()));

        if (file != null)
        {
            terminal.writeLine("");
            terminal.writeLine(Messages.keyValue("ファイル名", file.getName()));
            terminal.writeLine(Messages.keyValue("ダウンロードサイズ", Utils.roundSizeUnit(file.length())));
        }

        terminal.writeLine("");
        terminal.write(dependTree("依存関係", meta.getDependsOn().stream()
                .map(dep -> {
                    if (dep.getDependType() == DependType.HARD_DEPEND)
                        return ChatColor.DARK_GREEN + dep.getDependsOn();
                    else if (dep.getDependType() == DependType.SOFT_DEPEND)
                        return ChatColor.GREEN + dep.getDependsOn();
                    else
                        return ChatColor.RED + dep.getDependsOn();
                })
                .collect(Collectors.toList())));
        terminal.write(dependTree("被依存関係", meta.getDependedBy().stream()
                .map(dep -> {
                    if (dep.getDependType() == DependType.HARD_DEPEND)
                        return ChatColor.DARK_GREEN + dep.getPlugin();
                    else if (dep.getDependType() == DependType.SOFT_DEPEND)
                        return ChatColor.GREEN + dep.getPlugin();
                    else
                        return ChatColor.RED + dep.getPlugin();
                })
                .collect(Collectors.toList())));

        terminal.writeLine("");
        terminal.write(commandList(plugin.getDescription().getCommands()));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (args.length == 1)
            return Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                    .map(Plugin::getName).collect(Collectors.toList());
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.info";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("インストールされているプラグインの詳細を表示します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("pluginName", "string")
        };
    }
}
