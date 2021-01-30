package net.kunmc.lab.teamkunpluginmanager.install;

import com.g00fy2.versioncompare.Version;
import javafx.util.Pair;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.utils.GitHubURLBuilder;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import net.kunmc.lab.teamkunpluginmanager.utils.URLUtils;
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
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Installer
{

    /**
     * URlからぶちこむ！
     * @param sender 結果を表示する対象の人
     * @param url URL!!!
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
                atomicURL.set(KnownPlugins.getKnown(atomicURL.get()).url);
            else
            {
                finalSender.sendMessage(ChatColor.RED + "E: " + atomicURL.get() + " が見つかりません。");
                finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
                return new Pair<>("", "");
            }
        }

        atomicURL.set(GitHubURLBuilder.urlValidate(atomicURL.get())); //GitHubのURLを正規化

        finalSender.sendMessage("ファイルのダウンロード中...");

        Pair<Boolean, String> downloadResult = URLUtils.downloadFile(atomicURL.get());
        if (downloadResult.getValue().equals(""))
        {
            finalSender.sendMessage(ChatColor.RED + "E: ファイルのダウンロードに失敗しました。");
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            return new Pair<>("", "");
        }

        finalSender.sendMessage(Messages.getModifyMessage(Messages.ModifyType.ADD, downloadResult.getValue()));

        add++;

        finalSender.sendMessage("情報を読み込み中...");

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
            finalSender.sendMessage(ChatColor.RED + "E: 情報を読み込みませんでした。");
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            return new Pair<>("", "");
        }
        
        added.add(new Pair<>(downloadResult.getValue(), description.getName()));
        
        if (downloadResult.getKey())
        {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(description.getName());
            if (Bukkit.getPluginManager().isPluginEnabled(description.getName()) && new Version(plugin.getDescription().getVersion()).isHigherThan(description.getVersion()))
            {
                modify++;
                finalSender.sendMessage(Messages.getModifyMessage(Messages.ModifyType.MODIFY,
                        plugin.getName() + ":" + plugin.getDescription().getVersion() +
                                " => " + description.getName() + ":" + description.getVersion()));
            }
            else
            {
                add--;
                finalSender.sendMessage("Already Up-to-date");
                if (new File(downloadResult.getValue()).exists())
                {
                    try
                    {
                        File f = new File(downloadResult.getValue());
                        f.setWritable(true);
                        f.delete();
                    }
                    catch (Exception e)
                    {
                        finalSender.sendMessage(ChatColor.RED + "E: ファイルの削除に失敗しました：" + downloadResult.getValue());
                    }
                }
            }
        }

        boolean dependFirst = true;
        ArrayList<String> failedResolve = new ArrayList<>();
        for (String dependency: description.getDepend())
        {
            if (Bukkit.getPluginManager().isPluginEnabled(dependency))
                continue;
            if (dependFirst)
            {
                finalSender.sendMessage("依存関係をダウンロード中...");
                dependFirst = false;
            }

            String dependUrl = resolveDepend(dependency);
            if (dependUrl.equals("ERROR"))
            {
                failedResolve.add(dependency);
                continue;
            }

            Pair<String, String> dependResolve = Installer.install(null, dependUrl, true);
            if (!dependResolve.equals(""))
                failedResolve.add(dependency);
            else
            {
                finalSender.sendMessage(ChatColor.GREEN + "+ " + dependUrl.substring(dependUrl.lastIndexOf("/") + 1));
                added.add(dependResolve);
                add++;
            }

        }

        if (sender.equals(dummySender()) && failedResolve.size() > 0)
            return new Pair<>("", "");;

        if (failedResolve.size() > 0)
        {
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            finalSender.sendMessage(ChatColor.YELLOW + "W: " + description.getFullName() + " を正常にインストールしましたが、以下の依存関係のダウンロードに失敗しました。");
            finalSender.sendMessage(String.join(", ", failedResolve));
            return new Pair<>(downloadResult.getValue(), description.getName());
        }

        AtomicBoolean success = new AtomicBoolean(true);

        if (!ignoreInstall)
        {
            ArrayList<String> loadOrder = PluginUtil.mathLoadOrder(added);
            loadOrder.forEach(f -> {
                try
                {
                    Bukkit.getPluginManager().enablePlugin(Objects.requireNonNull(Bukkit.getPluginManager().loadPlugin(new File(f))));
                }
                catch (NullPointerException | InvalidPluginException | InvalidDescriptionException e)
                {
                    e.printStackTrace();
                    success.set(false);
                }
            });
            if (!success.get())
                finalSender.sendMessage(ChatColor.RED + "E: プラグインの読み込みに失敗しました。");
        }


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

    private static CommandSender dummySender()
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
