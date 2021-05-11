package net.kunmc.lab.teamkunpluginmanager.commands;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.Installer;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPluginEntry;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandUpdate
{
    public static void onCommand(CommandSender sender, String[] args)
    {
        if (sender != null && !sender.hasPermission("kpm.update"))
        {
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");
            return;
        }

        if (sender == null)
            sender = Installer.dummySender();

        if (!TeamKunPluginManager.plugin.isTokenAvailable())
        {
            sender.sendMessage(ChatColor.RED + "E: トークンがセットされていません！");
            sender.sendMessage(ChatColor.RED + "/kpm register でトークンを発行してください。");
            return;
        }

        if (!TeamKunPluginManager.session.lock())
        {
            sender.sendMessage(ChatColor.RED + "E: TeamKunPluginManagerが多重起動しています。");
            return;
        }

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "アップデートファイルのダウンロードを開始します...");
        CommandSender finalSender = sender;

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                ArrayList<String> paths = new ArrayList<>();
                AtomicInteger num = new AtomicInteger(0);
                new File(TeamKunPluginManager.DATABASE_PATH).mkdirs();
                TeamKunPluginManager.config.getMapList("config")
                        .forEach(s -> {
                            String fileName;

                            int i = 0;
                            do
                            {
                                fileName = TeamKunPluginManager.DATABASE_PATH + (++i) + ".json";
                            }
                            while (new File(fileName).exists());

                            String url = (String) s.get("url");
                            try
                            {
                                URL urlObj = new URL(url);
                                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                                connection.setRequestMethod("GET");
                                if (Boolean.parseBoolean(s.get("auth").toString()) && (urlObj.getHost().equals("api.github.com") ||
                                        urlObj.getHost().equals("raw.githubusercontent.com")))
                                    connection.setRequestProperty("Authorization", "token " + TeamKunPluginManager.vault.getToken());
                                connection.setRequestProperty("User-Agent",
                                        "Mozilla/8.10; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop");
                                connection.connect();
                                if (connection.getResponseCode() != 200)
                                {
                                    finalSender.sendMessage(ChatColor.RED + "無視：" + num.incrementAndGet() + " " + url);

                                    return;
                                }

                                File file = new File(fileName);
                                paths.add(fileName);
                                if (!file.createNewFile())
                                {
                                    finalSender.sendMessage(ChatColor.GREEN + "ヒット：" + num.incrementAndGet() + " " + url);
                                    return;
                                }
                                try(InputStream is = connection.getInputStream();
                                    OutputStream os = new FileOutputStream(file))
                                {
                                    IOUtils.copy(is, os);
                                }


                                finalSender.sendMessage(ChatColor.GREEN + "取得：" + num.incrementAndGet() +
                                        " " + url + " [" + PluginUtil.getFileSizeString(file.length()) + "]");
                            }
                            catch (MalformedURLException e)
                            {
                                finalSender.sendMessage(ChatColor.RED + "エラー：" + num.incrementAndGet() + " " + url);
                                finalSender.sendMessage(ChatColor.RED + "  '" + url + "' はURLではありません。");
                            }
                            catch (UnknownHostException e)
                            {
                                finalSender.sendMessage(ChatColor.RED + "エラー：" + num.incrementAndGet() + " " + url);
                                finalSender.sendMessage(ChatColor.RED + "  '" + url + "' を解決できませんでした。");
                            }
                            catch (Exception e)
                            {
                                finalSender.sendMessage(ChatColor.RED + "エラー：" + num.incrementAndGet() + " " + url);
                                finalSender.sendMessage(ChatColor.RED + "  エラー " + e.getClass().getName() + " が発生しました。");
                                e.printStackTrace();
                            }
                        });

                finalSender.sendMessage(ChatColor.GREEN + "ファイルのダウンロードに成功しました。");
                finalSender.sendMessage(ChatColor.LIGHT_PURPLE + "リストを読み込んでいます...");

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

                paths.stream().parallel().forEach(s -> {
                    if (new File(s).exists())
                    {
                        try
                        {
                            FileUtils.forceDelete(new File(s));
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

                finalSender.sendMessage(ChatColor.GREEN + "項目数: " + atomicInteger.get());
                finalSender.sendMessage(ChatColor.GREEN + "S: プラグイン定義ファイルのアップデートに成功しました。");
                TeamKunPluginManager.session.unlock();
            }
        }.runTaskAsynchronously(TeamKunPluginManager.plugin);
    }
}
