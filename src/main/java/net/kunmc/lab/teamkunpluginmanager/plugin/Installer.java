package net.kunmc.lab.teamkunpluginmanager.plugin;

import com.g00fy2.versioncompare.Version;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.utils.GitHubURLBuilder;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import net.kunmc.lab.teamkunpluginmanager.utils.Pair;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import net.kunmc.lab.teamkunpluginmanager.utils.URLUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public class Installer
{

    public static void unInstall(CommandSender sender, String name)
    {
        if (sender == null)
            sender = dummySender();
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "依存関係ツリーを読み込み中...");
        DependencyTree.Info info = DependencyTree.getInfo(name, false);
        if (info == null)
        {
            sender.sendMessage(ChatColor.RED + "E: プラグインが見つかりませんでした。");
            return;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin(info.name);

        if (!PluginUtil.isPluginLoaded(info.name))
        {
            sender.sendMessage(ChatColor.RED + "E: プラグインが見つかりませんでした。");
            return;
        }

        if (TeamKunPluginManager.config.getStringList("ignore").stream().anyMatch(s -> s.equalsIgnoreCase(info.name)))
        {
            sender.sendMessage(ChatColor.YELLOW + "W: このプラグインは保護されています。\n" +
                    ChatColor.YELLOW + "   保護されているプラグインを削除すると、サーバの動作に支障を来す可能性がございます。");
            sender.sendMessage(ChatColor.RED + "E: システムが保護されました。");
            return;
        }

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "プラグインを削除中...");

        PluginUtil.unload(plugin);
        CommandSender finalSender = sender;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                File file = PluginUtil.getFile(plugin);
                if (file != null)
                    file.delete();
                DependencyTree.wipePlugin(plugin);
                finalSender.sendMessage(ChatColor.RED + "- " + plugin.getName() + ":" + plugin.getDescription().getVersion());
                String statusError = Messages.getErrorMessage();
                if (!statusError.equals(""))
                    finalSender.sendMessage(statusError);

                String autoRemovable = Messages.getUnInstallableMessage();

                if (!autoRemovable.equals(""))
                    finalSender.sendMessage(autoRemovable);
                finalSender.sendMessage(Messages.getStatusMessage(0, 1, 0));
                finalSender.sendMessage(ChatColor.GREEN + "S: " + plugin.getName() + ":" + plugin.getDescription().getVersion() + " を正常にアンインストールしました。");
            }
        }.runTaskLaterAsynchronously(TeamKunPluginManager.plugin, 20L);
    }

    private static String error(String json)
    {
        try
        {
            JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
            if (!jsonObject.has("message"))
                return "";
            return jsonObject.get("message").getAsString();
        }
        catch (Exception ignored)
        {
            return "";
        }
    }

    /**
     * URlからぶちこむ！
     *
     * @param sender        結果を表示する対象の人
     * @param url           URL!!!
     * @param ignoreInstall インストールを除外するかどうか
     * @return ファイル名, プラグイン名
     */
    public static Pair<String, String> install(CommandSender sender, String url, boolean ignoreInstall)
    {
        if (sender == null)
            sender = dummySender();

        AtomicReference<String> atomicURL = new AtomicReference<>(url);
        CommandSender finalSender = sender;
        ArrayList<Pair<String, String>> added = new ArrayList<>();

        int add = 0;
        int remove = 0;
        int modify = 0;

        if (!UrlValidator.getInstance().isValid(atomicURL.get()))
        {
            if (StringUtils.split(atomicURL.get(), "/").length == 2)
                atomicURL.set("https://github.com/" + atomicURL.get());
            else if (KnownPlugins.isKnown(atomicURL.get()))
                atomicURL.set(Objects.requireNonNull(KnownPlugins.getKnown(atomicURL.get())).url);
            else
            {
                finalSender.sendMessage(ChatColor.RED + "E: " + atomicURL.get() + " が見つかりません。");
                finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
                return new Pair<>("", "");
            }
        }

        atomicURL.set(GitHubURLBuilder.urlValidate(atomicURL.get())); //GitHubのURLを正規化

        if (atomicURL.get().startsWith("ERROR "))
        {
            finalSender.sendMessage(ChatColor.RED + "E: " + atomicURL.get().substring(6)); //ERROR <-までをきりだし
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            return new Pair<>("", "");
        }
        finalSender.sendMessage(ChatColor.GOLD + "ファイルのダウンロード中...");

        long startTime = System.currentTimeMillis();

        Pair<Boolean, String> downloadResult = URLUtils.downloadFile(atomicURL.get());
        if (downloadResult.getValue().equals(""))
        {
            finalSender.sendMessage(ChatColor.RED + "E: ファイルのダウンロードに失敗しました。");
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            return new Pair<>("", "");
        }

        finalSender.sendMessage(Messages.getModifyMessage(Messages.ModifyType.ADD, downloadResult.getValue()));
        add++;

        finalSender.sendMessage(ChatColor.DARK_GREEN.toString() + new BigDecimal(String.valueOf(System.currentTimeMillis())).subtract(new BigDecimal(String.valueOf(startTime))).divide(new BigDecimal("1000")).setScale(2, BigDecimal.ROUND_DOWN) + "秒で取得しました。");

        finalSender.sendMessage(ChatColor.LIGHT_PURPLE + "情報を読み込み中...");

        PluginDescriptionFile description;

        try
        {
            description = PluginUtil.loadDescription(new File("plugins/" + downloadResult.getValue()));
        }
        catch (FileNotFoundException e)
        {
            finalSender.sendMessage(ChatColor.RED + "E: ファイルが見つかりませんでした。");
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            return new Pair<>("", "");
        }
        catch (IOException | InvalidDescriptionException e)
        {
            finalSender.sendMessage(ChatColor.RED + "E: 情報を読み込めませんでした。");
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            return new Pair<>("", "");
        }

        if (TeamKunPluginManager.config.getStringList("ignore").stream().anyMatch(s -> s.equalsIgnoreCase(description.getName())))
        {
            sender.sendMessage(ChatColor.RED + "E: このプラグインは保護されています。");
            add--;
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            return new Pair<>("", "");
        }

        DependencyTree.Info info = DependencyTree.getInfo(description.getName(), false);

        if (info != null && new Version(info.version).isHigherThan(description.getVersion()))
        {
            add--;
            finalSender.sendMessage(ChatColor.YELLOW + "W: 既に同じプラグインが存在します。");
            if (new File("plugins/" + downloadResult.getValue()).exists())
            {
                try
                {
                    File f = new File("plugins/" + downloadResult.getValue());
                    f.setWritable(true);
                    f.delete();
                }
                catch (Exception e)
                {
                    finalSender.sendMessage(ChatColor.RED + "E: ファイルの削除に失敗しました: " + downloadResult.getValue());
                }
            }
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            finalSender.sendMessage(ChatColor.GREEN + "S: " + description.getFullName() + " を正常にインストールしました。");
            return new Pair<>(downloadResult.getValue(), description.getName());
        }

        added.add(new Pair<>(downloadResult.getValue(), description.getName()));

        boolean dependFirst = true;
        ArrayList<String> failedResolve = new ArrayList<>();
        for (String dependency : description.getDepend())
        {
            if (Bukkit.getPluginManager().isPluginEnabled(dependency))
                continue;
            if (dependFirst)
            {
                finalSender.sendMessage(ChatColor.GOLD + "依存関係をダウンロード中...");
                startTime = System.currentTimeMillis();
                dependFirst = false;
            }

            String dependUrl = resolveDepend(dependency);
            if (dependUrl.equals("ERROR"))
            {
                failedResolve.add(dependency);
                continue;
            }

            Pair<String, String> dependResolve = Installer.install(null, dependUrl, true);
            if (dependResolve.getKey().equals(""))
                failedResolve.add(dependency);
            else
            {
                if (Bukkit.getPluginManager().getPlugin(dependUrl.substring(dependUrl.lastIndexOf("/"))) == null)
                    finalSender.sendMessage(ChatColor.GREEN + "+ " + dependUrl.substring(dependUrl.lastIndexOf("/") + 1));
                else
                {
                    Plugin dependPlugin = Bukkit.getPluginManager().getPlugin(dependUrl.substring(dependUrl.lastIndexOf("/")));
                    finalSender.sendMessage(ChatColor.GREEN + "+ " + dependPlugin.getName() + ":" + dependPlugin.getDescription().getVersion());
                }
                added.add(dependResolve);
                add++;
            }

        }

        if (downloadResult.getKey())
        {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(description.getName());
            if (Bukkit.getPluginManager().isPluginEnabled(description.getName()) && new Version(plugin.getDescription().getVersion()).isLowerThan(description.getVersion()))
            {
                modify++;
                add--;
                finalSender.sendMessage(Messages.getModifyMessage(
                        Messages.ModifyType.MODIFY,
                        plugin.getName() + ":" + plugin.getDescription().getVersion() +
                                " => " + description.getName() + ":" + description.getVersion()
                ));
            }
            else
            {
                add--;
                finalSender.sendMessage(ChatColor.YELLOW + "W: 既に同じプラグインが存在します。");
                if (new File("plugins/" + downloadResult.getValue()).exists())
                {
                    try
                    {
                        File f = new File("plugins/" + downloadResult.getValue());
                        f.setWritable(true);
                        f.delete();
                    }
                    catch (Exception e)
                    {
                        finalSender.sendMessage(ChatColor.RED + "E: ファイルの削除に失敗しました: " + downloadResult.getValue());
                    }
                }
                finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
                finalSender.sendMessage(ChatColor.GREEN + "S: " + description.getFullName() + " を正常にインストールしました。");
                return new Pair<>(downloadResult.getValue(), description.getName());

            }
        }


        finalSender.sendMessage(ChatColor.DARK_GREEN.toString() + new BigDecimal(String.valueOf(System.currentTimeMillis())).subtract(new BigDecimal(String.valueOf(startTime))).divide(new BigDecimal("1000")).setScale(2, BigDecimal.ROUND_DOWN) + "秒で取得しました。");
        if (sender.equals(dummySender()) && failedResolve.size() > 0)
            return new Pair<>("", "");

        if (failedResolve.size() > 0)
        {
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            finalSender.sendMessage(ChatColor.YELLOW + "W: " + description.getFullName() + " を正常にインストールしましたが、以下の依存関係の処理に失敗しました。");
            finalSender.sendMessage(ChatColor.RED + String.join(", ", failedResolve));
            return new Pair<>(downloadResult.getValue(), description.getName());
        }
        AtomicBoolean success = new AtomicBoolean(true);

        if (!ignoreInstall)
        {
            ArrayList<String> loadOrder = PluginUtil.mathLoadOrder(added);
            for (String f : loadOrder)
            {
                try
                {
                    if (downloadResult.getKey())
                    {
                        if (!PluginUtil.isPluginLoaded(description.getName()))
                        {
                            finalSender.sendMessage(ChatColor.RED + "E: Bukkitのインジェクションに失敗しました。");
                            success.set(false);
                            continue;
                        }
                        JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(description.getName());

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
                        }.runTaskLaterAsynchronously(TeamKunPluginManager.plugin, 1000L);
                    }

                    PluginUtil.load(f.substring(0, f.length() - 4));
                }
                catch (Exception e)
                {
                    try
                    {
                        new File("plugins/" + f).setWritable(true);
                        new File("plugins/" + f).delete();
                    }
                    catch (Exception ex)
                    {
                        finalSender.sendMessage(ChatColor.RED + "E: ファイルの削除に失敗しました: " + downloadResult.getValue());
                    }
                    e.printStackTrace();
                    success.set(false);
                }
            }
        }
        if (!success.get())
            finalSender.sendMessage(ChatColor.RED + "E: プラグインの読み込みに失敗しました。");


        String statusError = Messages.getErrorMessage();
        if (!statusError.equals(""))
            sender.sendMessage(statusError);
        String autoRemovable = Messages.getUnInstallableMessage();
        if (!autoRemovable.equals(""))
            sender.sendMessage(autoRemovable);

        finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
        finalSender.sendMessage(ChatColor.GREEN + "S: " + description.getFullName() + " を正常にインストールしました。");
        return new Pair<>(downloadResult.getValue(), description.getName());
    }

    public static String resolveDepend(String name)
    {
        if (KnownPlugins.isKnown(name))
            return KnownPlugins.getKnown(name).url;

        String orgName = TeamKunPluginManager.config.getString("gitHubName");

        String gitHubRepo = orgName + "/" + name;

        String repository = GitHubURLBuilder.urlValidate("https://github.com/" + gitHubRepo);

        return repository.startsWith("ERROR") ? "ERROR": gitHubRepo;
    }

    public static String[] getRemovableDataDirs()
    {
        try
        {
            List<String> bb = TeamKunPluginManager.config.getStringList("ignore");

            return Arrays.stream(Objects.requireNonNull(new File("plugins/").listFiles(File::isDirectory)))
                    .map(File::getName)
                    .filter(file -> !PluginUtil.isPluginLoaded(file))
                    .filter(file -> !bb.contains(file))
                    .toArray(String[]::new);

        }
        catch (Exception e)
        {
            return new String[]{};
        }
    }

    /**
     * プラグインデータフォルダを削除
     *
     * @param name 対象
     * @return 合否
     */
    public static boolean clean(String name)
    {
        if (DependencyTree.isErrors())
            return false;

        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);

        if (PluginUtil.isPluginLoaded(name))
            return false;  //プラグインがイネーブルの時、プロセスロックが掛かる

        if (TeamKunPluginManager.config.getStringList("ignore").stream()
                .anyMatch(s -> s.equalsIgnoreCase(name))) // 保護されているかどうか
            return false;

        if (plugin != null)
            return plugin.getDataFolder().delete(); //プラグインがあった場合、データフォルダを取得して削除

        //プラグインがなかった場合 <= 厄介

        try
        {
            Arrays.stream(Objects.requireNonNull(new File("plugins/")
                    .listFiles(File::isDirectory)))
                    .filter(file -> file.getName().equalsIgnoreCase(name))
                    .forEach(file -> {
                        try
                        {
                            FileUtils.forceDelete(file);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    });
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static CommandSender dummySender()
    {
        return new CommandSender()
        {

            @Override
            public void sendMessage(String message)
            {

            }

            @Override
            public void sendMessage(String[] messages)
            {

            }

            @Override
            public Server getServer()
            {
                return Bukkit.getServer();
            }

            @Override
            public String getName()
            {
                return "DUMMY1145141919810931";
            }

            @Override
            public Spigot spigot()
            {
                return spigot();
            }

            @Override
            public boolean isPermissionSet(String name)
            {
                return false;
            }

            @Override
            public boolean isPermissionSet(Permission perm)
            {
                return false;
            }

            @Override
            public boolean hasPermission(String name)
            {
                return false;
            }

            @Override
            public boolean hasPermission(Permission perm)
            {
                return false;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value)
            {
                return null;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin)
            {
                return null;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks)
            {
                return null;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, int ticks)
            {
                return null;
            }

            @Override
            public void removeAttachment(PermissionAttachment attachment)
            {

            }

            @Override
            public void recalculatePermissions()
            {

            }

            @Override
            public Set<PermissionAttachmentInfo> getEffectivePermissions()
            {
                return null;
            }

            @Override
            public boolean isOp()
            {
                return false;
            }

            @Override
            public void setOp(boolean value)
            {

            }
        };
    }

}