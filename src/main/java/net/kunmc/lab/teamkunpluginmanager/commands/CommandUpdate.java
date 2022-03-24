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
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

        if (!TeamKunPluginManager.getPlugin().isTokenAvailable())
        {
            sender.sendMessage(ChatColor.RED + "E: トークンがセットされていません！");
            sender.sendMessage(ChatColor.RED + "/kpm register でトークンを発行してください。");
            return;
        }

        if (!TeamKunPluginManager.getPlugin().getSession().lock())
        {
            sender.sendMessage(ChatColor.RED + "E: TeamKunPluginManagerが多重起動しています。");
            return;
        }

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "エイリアスセットファイルのダウンロードを開始します...");
        CommandSender finalSender = sender;

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                HashMap<String, String> paths = new HashMap<>();
                new File(TeamKunPluginManager.DATABASE_PATH).mkdirs();

                DownloadResult downloadResult = doDownload(finalSender);
                printDownloadMessage(finalSender, downloadResult);
                finalSender.sendMessage(ChatColor.LIGHT_PURPLE + "リストを読み込んでいます...");

                int aliases = doRegister(downloadResult);

                doCleanUp(downloadResult);

                if (downloadResult.errors)
                    finalSender.sendMessage(ChatColor.YELLOW + "W: いくつかのエイリアスセットファイルのダウンロードに失敗しました。" +
                            "これらは無視されるか、古いものが代わりに使われます。");
                finalSender.sendMessage(ChatColor.GREEN + "項目数: " + aliases);
                finalSender.sendMessage(ChatColor.GREEN + "S: プラグイン定義ファイルのアップデートに成功しました。");
                TeamKunPluginManager.getPlugin().getSession().unlock();
            }
        }.runTaskAsynchronously(TeamKunPluginManager.getPlugin());
    }

    private static DownloadResult doDownload(CommandSender finalSender)
    {
        AtomicInteger num = new AtomicInteger(0);
        Map<String, String> paths = new HashMap<>();
        AtomicBoolean error = new AtomicBoolean(false);
        long start = System.currentTimeMillis();
        TeamKunPluginManager.getPlugin().getConfig().getMapList("config")
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
                            connection.setRequestProperty(
                                    "Authorization",
                                    "token " + TeamKunPluginManager.getPlugin().getVault().getToken()
                            );
                        connection.setRequestProperty(
                                "User-Agent",
                                "Mozilla/8.10; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop"
                        );
                        connection.connect();
                        if (connection.getResponseCode() != 200)
                        {
                            finalSender.sendMessage(ChatColor.RED + "無視：" + num.incrementAndGet() + " " + url);
                            finalSender.sendMessage(ChatColor.RED + "  " +
                                    connection.getResponseCode() + "  " +
                                    connection.getResponseMessage() + " [IP: " +
                                    InetAddress.getByName(urlObj.getHost()).getHostAddress() +
                                    "]");
                            error.set(true);
                            return;
                        }

                        File file = new File(fileName);

                        String name = (String) s.get("name");

                        if (paths.containsKey(name))
                        {
                            finalSender.sendMessage(ChatColor.RED + "エラー：" + num.incrementAndGet() + " " + name);
                            finalSender.sendMessage(ChatColor.RED + "  エイリアスセット '" + name + "' が重複しています。");
                            error.set(true);
                            return;
                        }
                        paths.put(name, fileName);
                        if (!file.createNewFile())
                        {
                            finalSender.sendMessage(ChatColor.GREEN + "ヒット：" + num.incrementAndGet() + " " + url);
                            return;
                        }
                        try (InputStream is = connection.getInputStream();
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
                        error.set(true);
                    }
                    catch (UnknownHostException e)
                    {
                        finalSender.sendMessage(ChatColor.RED + "エラー：" + num.incrementAndGet() + " " + url);
                        finalSender.sendMessage(ChatColor.RED + "  '" + url + "' を解決できませんでした。");
                        error.set(true);
                    }
                    catch (Exception e)
                    {
                        finalSender.sendMessage(ChatColor.RED + "エラー：" + num.incrementAndGet() + " " + url);
                        finalSender.sendMessage(ChatColor.RED + "  エラー " + e.getClass().getName() + " が発生しました。");
                        error.set(true);
                        e.printStackTrace();
                    }
                });

        return new DownloadResult(paths, error.get(), System.currentTimeMillis() - start);
    }

    private static void printDownloadMessage(CommandSender sender, DownloadResult result)
    {
        AtomicLong size = new AtomicLong(0);

        result.lists.values().stream().parallel().forEach(s -> {
            size.addAndGet(new File(s).length());
        });

        double seconds = new BigDecimal(result.sec).divide(new BigDecimal("1000")).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();


        sender.sendMessage(
                ChatColor.LIGHT_PURPLE +
                        PluginUtil.getFileSizeString(size.get()) +
                        " を " +
                        seconds +
                        "秒 で取得しました (" +
                        PluginUtil.getFileSizeString(size.get() / ((long) seconds == 0 ? 1: (long) seconds)) +
                        "/s)"
        );

    }

    private static void doCleanUp(DownloadResult result)
    {
        result.lists.values().stream().parallel().forEach(s -> {
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
    }

    private static int doRegister(DownloadResult result)
    {
        AtomicInteger atomicInteger = new AtomicInteger(0);

        result.lists.entrySet().stream().parallel()
                .forEach(s -> {
                    JsonObject obj;
                    try (BufferedReader reader = new BufferedReader(new FileReader(s.getValue())))
                    {
                        obj = new Gson().fromJson(reader, JsonObject.class);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        return;
                    }

                    String source = s.getKey();

                    KnownPlugins.del(source);
                    for (Map.Entry<String, JsonElement> a : obj.entrySet())
                    {
                        String name = a.getKey();
                        String url = a.getValue().getAsString();
                        KnownPluginEntry ent = new KnownPluginEntry(name, url, source);
                        KnownPlugins.addKnownPlugin(ent);
                        atomicInteger.incrementAndGet();
                    }
                });

        return atomicInteger.get();
    }

    private static class DownloadResult
    {
        public DownloadResult(Map<String, String> lists, boolean errors, long sec)
        {
            this.lists = lists;
            this.errors = errors;
            this.sec = sec;
        }

        public Map<String, String> lists;
        public boolean errors;
        public long  sec;
    }
}
