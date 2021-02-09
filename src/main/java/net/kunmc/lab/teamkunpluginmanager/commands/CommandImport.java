package net.kunmc.lab.teamkunpluginmanager.commands;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.Installer;
import net.kunmc.lab.teamkunpluginmanager.plugin.compactor.PluginContainer;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import net.kunmc.lab.teamkunpluginmanager.utils.URLUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandImport
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("kpm.import"))
        {
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");
            return;
        }
        if (args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "E: 引数が不足しています！");
            sender.sendMessage(ChatColor.RED + "使用法: /kpm import URL");
            return;
        }

        String url = args[0];
        AtomicInteger add = new AtomicInteger();
        AtomicInteger remove = new AtomicInteger();
        AtomicInteger modify = new AtomicInteger();

        if (!UrlValidator.getInstance().isValid(url))
        {
            sender.sendMessage(ChatColor.RED + "E: 第一引数に適切なURLを入力してください。");
            sender.sendMessage(Messages.getStatusMessage(add.get(), remove.get(), modify.get()));

            return;
        }

        sender.sendMessage(ChatColor.GOLD + "ファイルのダウンロード中...");

        String json = URLUtils.getAsString(url);

        sender.sendMessage(ChatColor.GOLD + "ファイルの読み込み中...");

        LinkedList<PluginContainer> container;
        try
        {
            container = new Gson().fromJson(json, new TypeToken<LinkedList<PluginContainer>>()
            {
            }.getType());
        }
        catch (JsonParseException e)
        {
            sender.sendMessage(ChatColor.RED + "E: JSONファイルが正しくないようです。");
            return;
        }

        ArrayList<InstallResult> results = new ArrayList<>();


        container.stream().parallel()
                .forEach(pluginContainer -> {
                    InstallResult result = Installer.install(null, pluginContainer.downloadUrl, true, true, true);
                    add.addAndGet(result.add);
                    remove.addAndGet(result.remove);
                    modify.addAndGet(result.modify);
                    results.add(result);
                });


        ArrayList<InstallResult> loadOrder = PluginUtil.mathLoadOrder(results);

        TeamKunPluginManager.enableBuildTree = false;

        sender.sendMessage(ChatColor.GOLD + "設定を書き込み中...");

        container.stream().parallel().forEach(pluginContainer -> {
            if (pluginContainer.config == null || pluginContainer.config.size() == 0)
                return;
            Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginContainer.pluginName);
            if (!PluginUtil.isPluginLoaded(plugin))
                return;


            try
            {
                FileUtils.writeStringToFile(new File(plugin.getDataFolder(), "config.yml"), new Yaml().dump(pluginContainer.config), StandardCharsets.UTF_8, false);

            }
            catch (IOException e)
            {
                e.printStackTrace();
                sender.sendMessage(ChatColor.RED + "プラグイン '" + pluginContainer.pluginName + "' の設定の保存に失敗しました。");
            }
        });

        loadOrder.forEach(installResult -> {
            if (!installResult.success)
                return;
            if (PluginUtil.isPluginLoaded(installResult.pluginName))
            {
                JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(installResult.pluginName);

                PluginUtil.unload(plugin);

                new BukkitRunnable()
                {

                    @Override
                    public void run()
                    {
                        File file = PluginUtil.getFile(plugin);
                        if (file != null)
                            file.delete();
                    }
                }.runTaskLaterAsynchronously(TeamKunPluginManager.plugin, 20L);
            }
            PluginUtil.load(installResult.fileName.substring(0, installResult.fileName.length() - 4));
            sender.sendMessage(ChatColor.GREEN + "+ " + installResult.pluginName);
        });
        TeamKunPluginManager.enableBuildTree = true;
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "依存関係ツリーを構築中...");
        container.parallelStream().forEach(pluginContainer -> {
            DependencyTree.crawlPlugin(pluginContainer.pluginName);
        });


        sender.sendMessage(ChatColor.GREEN + "S: 正常にインポートしました。");
        sender.sendMessage(Messages.getStatusMessage(add.get(), remove.get(), modify.get()));

    }
}
