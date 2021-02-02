package net.kunmc.lab.teamkunpluginmanager.commands;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.install.Installer;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPluginEntry;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPlugins;
import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandUpdate
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (sender != null && !sender.hasPermission("kpm.update"))
        {
            sender.sendMessage(ChatColor.RED + "E：権限がありません！");
            return;
        }


        if (sender == null)
            sender = Installer.dummySender();

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "アップデートファイルのダウンロードを開始します...");
        CommandSender finalSender = sender;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                ArrayList<String> paths = new ArrayList<>();
                TeamKunPluginManager.config.getStringList("config")
                        .forEach(s -> {
                            String fileName;

                            int i = 0;
                            do
                            {
                                fileName = TeamKunPluginManager.DATABASE_PATH + (++i) + ".json";
                            }
                            while (new File(fileName).exists());

                            try
                            {
                                URL urlObj = new URL(s);
                                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                                connection.setRequestMethod("GET");
                                if (urlObj.getHost().equals("api.github.com"))
                                    connection.setRequestProperty("Authorization", "token " + TeamKunPluginManager.config.getString("oauth"));
                                connection.setRequestProperty("User-Agent", "TeamKUN Client");
                                connection.connect();
                                if (connection.getResponseCode() != 200)
                                {
                                    finalSender.sendMessage(ChatColor.RED + "ファイルのダウンロード失敗しました。");
                                    return;
                                }

                                File file = new File(fileName);
                                paths.add(fileName);
                                FileUtils.copyURLToFile(urlObj, file);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        });

                finalSender.sendMessage(ChatColor.GREEN + "ファイルのダウンロードに成功しました。");
                finalSender.sendMessage(ChatColor.LIGHT_PURPLE + "データベースに登録しています...");

                AtomicInteger atomicInteger = new AtomicInteger(0);

                KnownPlugins.drop();
                paths.stream().parallel()
                        .forEach(s -> {
                            JsonObject obj;
                            try (BufferedReader reader = new BufferedReader(new FileReader(s)))
                            {
                                obj = new Gson().fromJson(reader, JsonObject.class);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                return;
                            }

                            for (Map.Entry<String, JsonElement> a : obj.entrySet())
                            {
                                String name = a.getKey();
                                String url = a.getValue().getAsString();
                                KnownPluginEntry ent = new KnownPluginEntry();
                                ent.name = name;
                                ent.url = url;
                                KnownPlugins.addKnownPlugin(ent);
                                atomicInteger.incrementAndGet();
                            }
                        });

                finalSender.sendMessage(ChatColor.GREEN + "項目数： " + atomicInteger.get());
                finalSender.sendMessage(ChatColor.GREEN + "S: 既知プラグインデータセットのアップデートに成功しました。");
            }
        }.runTaskAsynchronously(TeamKunPluginManager.plugin);
    }
}
