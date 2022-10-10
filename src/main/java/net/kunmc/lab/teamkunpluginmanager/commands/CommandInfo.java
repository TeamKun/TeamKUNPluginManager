package net.kunmc.lab.teamkunpluginmanager.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.KPMDaemon;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.meta.DependType;
import net.kunmc.lab.teamkunpluginmanager.plugin.meta.InstallOperator;
import net.kunmc.lab.teamkunpluginmanager.plugin.meta.PluginMeta;
import net.kunmc.lab.teamkunpluginmanager.plugin.meta.PluginMetaProvider;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CommandInfo extends CommandBase
{
    private final KPMDaemon daemon;

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
        Component component = Component.text(keyValue("コマンド名", name + "\n\n"));

        if (command.containsKey("aliases"))
        {
            if (command.get("aliases") instanceof String)
                component = component.append(Component.text(keyValue("エイリアス", "/" + command.get("aliases"))));
            else if (command.get("aliases") instanceof List)
                component = component.append(Component.text(keyValue(
                        "エイリアス",
                        "/" + String.join(", /", (List<String>) command.get("aliases"))
                ) + "\n"));
        }

        if (command.containsKey("usage"))
            component = component.append(Component.text(keyValue("使用法", command.get("usage")) + "\n"));
        if (command.containsKey("description"))
            component = component.append(Component.text(keyValue("概要", command.get("description")) + "\n"));
        if (command.containsKey("permission"))
            component = component.append(Component.text(keyValue("権限", command.get("permission")) + "\n"));
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

    private static String epochToString(long epoch)
    {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(epoch));
    }

    private static String getInstalledByString(InstallOperator operator)
    {
        switch (operator)
        {
            case SERVER_ADMIN:
                return ChatColor.GREEN + "サーバー管理者";
            case KPM_PLUGIN_UPDATER:
                return ChatColor.AQUA + "KPMプラグインアップデーター";
            case KPM_DEPENDENCY_RESOLVER:
                return ChatColor.DARK_AQUA + "KPM依存関係解決機能";
            case OTHER:
                return ChatColor.RED + "その他";
            default:
                return ChatColor.GRAY + "不明";
        }
    }

    private static String keyValue(String property, String value)
    {
        return ChatColor.GREEN + property + ChatColor.WHITE + ": " + ChatColor.DARK_GREEN + value;
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

    private static String keyValueYesNo(String property, boolean a)
    {
        return ChatColor.GREEN + property + ChatColor.WHITE + ": " + (a ? ChatColor.DARK_GREEN + "はい": ChatColor.RED + "いいえ");
    }

    private static String keyValue(String property, Object obj)
    {
        return ChatColor.GREEN + property + ChatColor.WHITE + ": " + obj.toString();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1, 1))
            return;

        terminal.info("依存関係ツリーを読み込み中...");

        JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(args[0]);

        if (plugin == null)
        {
            terminal.error("情報の読み込みに失敗しました。");
            return;
        }

        PluginMetaProvider provider = this.daemon.getPluginMetaManager().getProvider();
        if (!provider.isPluginMetaExists(plugin.getName()))
        {
            terminal.error("プラグインが見つかりませんでした。");
            return;
        }

        PluginMeta meta = provider.getPluginMeta(
                plugin.getName(), true, true
        );

        terminal.info("情報を読み込み中...");
        File file = PluginUtil.getFile(plugin);

        terminal.writeLine(keyValue("名前", meta.getName()));
        terminal.writeLine(keyValue("作成者", String.join(", ", meta.getAuthors())));
        terminal.writeLine(keyValue("状態", plugin.isEnabled() ? ChatColor.DARK_GREEN + "有効": ChatColor.RED + "無効"));
        terminal.writeLine(keyValue("読み込みタイミング", PluginUtil.loadToString(plugin.getDescription().getLoad())));
        terminal.writeLine(keyValueYesNo(
                "保護",
                TeamKunPluginManager.getPlugin().getPluginConfig().getStringList("ignore").stream().parallel()
                        .anyMatch(s -> s.equalsIgnoreCase(meta.getName()))
        ));

        if (plugin.getDescription().getWebsite() != null)
            terminal.writeLine(keyValue("ウェブサイト", ChatColor.UNDERLINE + plugin.getDescription().getWebsite()));
        if (plugin.getDescription().getPrefix() != null)
            terminal.writeLine(keyValue("ログ接頭辞", plugin.getDescription().getPrefix()));
        if (plugin.getDescription().getDescription() != null)
            terminal.writeLine(keyValue("概要", plugin.getDescription().getDescription()));

        if (file != null)
        {
            terminal.writeLine("");
            terminal.writeLine(keyValue("ファイル名", file.getName()));
            terminal.writeLine(keyValue("ダウンロードサイズ", Utils.roundSizeUnit(file.length())));
        }

        terminal.writeLine("");
        terminal.writeLine(keyValue(
                "インストール者",
                getInstalledByString(meta.getInstalledBy())
        ));
        terminal.writeLine(keyValueYesNo("依存関係?", meta.isDependency()));
        terminal.writeLine(keyValue("インストール日時", epochToString(meta.getInstalledAt())));


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
}
