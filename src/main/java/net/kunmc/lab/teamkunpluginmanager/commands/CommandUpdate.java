package net.kunmc.lab.teamkunpluginmanager.commands;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPluginEntry;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CommandUpdate extends CommandBase // TODO: Rewrite this
{
    private static DownloadResult doDownload(Terminal terminal)
    {
        AtomicInteger num = new AtomicInteger(0);
        Map<String, String> paths = new HashMap<>();
        AtomicBoolean error = new AtomicBoolean(false);
        long start = System.currentTimeMillis();
        TeamKunPluginManager.getPlugin().getPluginConfig().getMapList("config")
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
                            terminal.writeLine(ChatColor.RED + "無視：" + num.incrementAndGet() + " " + url);
                            terminal.writeLine(ChatColor.RED + "  " +
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
                            terminal.writeLine(ChatColor.RED + "エラー：" + num.incrementAndGet() + " " + name);
                            terminal.writeLine(ChatColor.RED + "  エイリアスセット '" + name + "' が重複しています。");
                            error.set(true);
                            return;
                        }
                        paths.put(name, fileName);
                        if (!file.createNewFile())
                        {
                            terminal.writeLine(ChatColor.GREEN + "ヒット：" + num.incrementAndGet() + " " + url);
                            return;
                        }
                        try (InputStream is = connection.getInputStream();
                             OutputStream os = new FileOutputStream(file))
                        {
                            IOUtils.copy(is, os);
                        }


                        terminal.writeLine(ChatColor.GREEN + "取得：" + num.incrementAndGet() +
                                " " + url + " [" + PluginUtil.getFileSizeString(file.length()) + "]");
                    }
                    catch (MalformedURLException e)
                    {
                        terminal.writeLine(ChatColor.RED + "エラー：" + num.incrementAndGet() + " " + url);
                        terminal.writeLine(ChatColor.RED + "  '" + url + "' はURLではありません。");
                        error.set(true);
                    }
                    catch (UnknownHostException e)
                    {
                        terminal.writeLine(ChatColor.RED + "エラー：" + num.incrementAndGet() + " " + url);
                        terminal.writeLine(ChatColor.RED + "  '" + url + "' を解決できませんでした。");
                        error.set(true);
                    }
                    catch (Exception e)
                    {
                        terminal.writeLine(ChatColor.RED + "エラー：" + num.incrementAndGet() + " " + url);
                        terminal.writeLine(ChatColor.RED + "  エラー " + e.getClass().getName() + " が発生しました。");
                        error.set(true);
                        e.printStackTrace();
                    }
                });

        return new DownloadResult(paths, error.get(), System.currentTimeMillis() - start);
    }

    private static void printDownloadMessage(Terminal terminal, DownloadResult result)
    {
        AtomicLong size = new AtomicLong(0);

        result.lists.values().stream().parallel().forEach(s -> {
            size.addAndGet(new File(s).length());
        });

        double seconds = new BigDecimal(result.sec).divide(new BigDecimal("1000"))
                .setScale(2, BigDecimal.ROUND_DOWN).doubleValue();


        terminal.writeLine(
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

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (!TeamKunPluginManager.getPlugin().getSession().lock())
        {
            sender.sendMessage(ChatColor.RED + "E: TeamKunPluginManagerが多重起動しています。");
            return;
        }

        if (!TeamKunPluginManager.getPlugin().isTokenAvailable())
        { // TODO: Set level to warn
            terminal.error("トークンが設定されていません！");
            terminal.info("/kpm register でトークンを発行することができます！");
            TeamKunPluginManager.getPlugin().getSession().unlock();
            return;
        }

        terminal.info("プラグインデータセットファイルのダウンロードを開始します...");
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                new File(TeamKunPluginManager.DATABASE_PATH).mkdirs();

                DownloadResult downloadResult = doDownload(terminal);
                printDownloadMessage(terminal, downloadResult);
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "リストを読み込んでいます...");

                int aliases = doRegister(downloadResult);

                doCleanUp(downloadResult);

                if (downloadResult.errors)
                    terminal.warn("いくつかのエイリアスセットファイルのダウンロードに失敗しました。" +
                            "これらは無視されるか、古いものが代わりに使われます。");
                terminal.writeLine(ChatColor.GREEN + "項目数: " + aliases);
                terminal.success("プラグイン定義ファイルのアップデートに成功しました。");
                TeamKunPluginManager.getPlugin().getSession().unlock();
            }
        }.runTaskAsynchronously(TeamKunPluginManager.getPlugin());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.update";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("既知プラグインデータセットをアップデートします。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
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
