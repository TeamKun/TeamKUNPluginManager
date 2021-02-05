package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.compactor.PluginCompacter;
import net.kunmc.lab.teamkunpluginmanager.plugin.compactor.PluginPreCompacter;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import net.kunmc.lab.teamkunpluginmanager.utils.Say2Functional;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandExport
{
    public static final UUID CONSOLE_UUID = UUID.randomUUID();
    public static HashMap<UUID, PluginPreCompacter> session = new HashMap<>();

    public static void onCommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("kpm.export"))
        {
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");
            return;
        }

        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "E: 引数が不足しています！");
            sender.sendMessage(ChatColor.RED + "使用法: /kpm export <all|プラグイン名> [プラグイン名2]...");
            return;
        }

        if (DependencyTree.isErrors())
        {
            sender.sendMessage(Messages.getErrorMessage());
            sender.sendMessage(ChatColor.RED + "E: エラーが検出されたため、システムが保護されました。");
            return;
        }

        UUID sessionKey = sender instanceof Player ? ((Player) sender).getUniqueId(): CONSOLE_UUID;

        if (session.containsKey(sessionKey))
        {
            sender.sendMessage(ChatColor.RED + "E: 既にエクスポートセッションが有効です。");
            return;
        }

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "情報を変換中...");

        args = Arrays.stream(args).parallel().map(String::toLowerCase).toArray(String[]::new);

        String[] validPlugin;
        if (args[0].equals("all"))
            validPlugin = Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).toArray(String[]::new);
        else
        {
            String[] finalArgs = args;
            validPlugin = Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                    .map(Plugin::getName)
                    .map(String::toLowerCase)
                    .filter(name -> containsIgnoreCase(finalArgs, name))
                    .toArray(String[]::new);
        }

        if (validPlugin.length == 0)
        {
            sender.sendMessage(ChatColor.RED + "E: エクスポート可能なプラグインが見つかりませんでした。");
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "この操作で、以下のプラグインがエクスポートされます: ");
        sender.sendMessage(ChatColor.GREEN + Arrays.stream(validPlugin).map(s -> Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(s)).getName()).collect(Collectors.joining(" ")));

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "依存関係ツリーを読み込み中...");

        String[] finalValidPlugin = validPlugin;
        String[] dependencies = Arrays.stream(validPlugin)
                .flatMap(plugin -> DependencyTree.getInfo(plugin, false).depends.stream())
                .map(depend -> depend.depend.toLowerCase())
                .filter(s -> !containsIgnoreCase(Arrays.stream(finalValidPlugin).map(String::toLowerCase).toArray(String[]::new), s))
                .distinct()
                .toArray(String[]::new);

        if (dependencies.length != 0)
        {
            sender.sendMessage(ChatColor.GREEN + "また、追加で以下のプラグインがエクスポートされます。");
            sender.sendMessage(ChatColor.GREEN + Arrays.stream(dependencies).map(s -> Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(s)).getName()).collect(Collectors.joining(" ")));
        }


        validPlugin = (String[]) ArrayUtils.addAll(validPlugin, dependencies);

        validPlugin = Arrays.stream(validPlugin).map(s -> Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(s)).getName()).toArray(String[]::new);

        sender.sendMessage("プラグインをバンドル中...");

        PluginPreCompacter compacter = new PluginPreCompacter();
        compacter.addAll(validPlugin);
        session.put(sender instanceof Player ? ((Player) sender).getUniqueId(): CONSOLE_UUID, compacter);
        fixError(sender instanceof Player ? ((Player) sender).getUniqueId(): CONSOLE_UUID, null);
        //fixErrorに引き継ぎする
    }

    private static void fixError(UUID uuid, String name)
    {
        PluginPreCompacter ppc = session.get(uuid);
        if (ppc == null)
            return;
        if (!ppc.isErrors())
        {
            runExec(uuid, ppc);
            return;
        }

        CommandSender sender = uuid == CONSOLE_UUID ? Bukkit.getConsoleSender(): Bukkit.getPlayer(CONSOLE_UUID);

        if (sender == null)
        {
            session.remove(uuid);
            return;
        }

        final String target;
        if (name == null)
            target = ppc.nextUrlError();
        else
            target = name;


        sender.sendMessage(ChatColor.RED + "プラグイン " +
                ChatColor.GOLD + "'" + target + "' " +
                ChatColor.RED + "のダウンロードURLの解決に失敗しました。");
        if (uuid == CONSOLE_UUID)
            sender.sendMessage(ChatColor.GREEN + "これを解決するには、コンソールでURLを発言してください。");
        else
            sender.sendMessage(ChatColor.GREEN + "これを解決するには、チャットでURLを発言してください。");

        TeamKunPluginManager.functional.add(uuid == CONSOLE_UUID ? null: uuid, new Say2Functional.FunctionalEntry(String::startsWith, s -> {
            ppc.fixUrl(target, s);
            sender.sendMessage(ChatColor.GREEN + "プラグイン " +
                    ChatColor.GOLD + "'" + target + "' " +
                    ChatColor.GREEN + "のダウンロードURLを解決しました。");
            fixError(uuid, ppc.nextUrlError());
        }));
    }

    private static void runExec(UUID uuid, PluginPreCompacter ppc)
    {
        CommandSender sender = uuid == CONSOLE_UUID ? Bukkit.getConsoleSender(): Bukkit.getPlayer(CONSOLE_UUID);

        if (sender == null)
        {
            session.remove(uuid);
            return;
        }

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "情報を読み込み中...");

        PluginCompacter pc = ppc.getCompacter();

        /*sender.sendMessage(ChatColor.LIGHT_PURPLE + "プラグインを適正化中...");
        pc.apply(pc.builder().build());
*/
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "データを書き込み中...");

        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddhhmmss");

        File exportAs = new File(
                TeamKunPluginManager.plugin.getDataFolder(),
                "exports/" + f.format(new Date()) + ".pmx"
        );

        String json = pc.build();
        try
        {
            FileUtils.writeStringToFile(exportAs, json, StandardCharsets.UTF_8, false);
        }
        catch (IOException e)
        {
            sender.sendMessage(ChatColor.RED + "E: データの書き込みに失敗しました。");
            session.remove(uuid);
            e.printStackTrace();
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "S: プラグインのエクスポートに成功しました。");
        session.remove(uuid);
    }

    public static boolean containsIgnoreCase(String[] target, String maf)
    {
        return Arrays.stream(target).parallel().anyMatch(maf::equalsIgnoreCase);
    }
}
