package net.kunmc.lab.teamkunpluginmanager.install;

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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class Installer
{

    /**
     * URlからぶちこむ！
     * @param url URL!!!
     * @return 解決に失敗したかどうか
     */
    public static boolean install(CommandSender sender, String url)
    {
        if (sender == null)
            sender = dummySender();

        AtomicReference<String> atomicURL = new AtomicReference<>(url);
        CommandSender finalSender = sender;

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
                return false;
            }
        }

        atomicURL.set(GitHubURLBuilder.urlValidate(atomicURL.get())); //GitHubのURLを正規化

        finalSender.sendMessage("ファイルのダウンロード中...");

        String fileName = URLUtils.downloadFile(atomicURL.get());
        if (fileName.equals(""))
        {
            finalSender.sendMessage(ChatColor.RED + "E: ファイルのダウンロードに失敗しました。");
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            return false;
        }

        finalSender.sendMessage(Messages.getModifyMessage(Messages.ModifyType.ADD, fileName));
        add++;

        finalSender.sendMessage(ChatColor.GREEN + "S: ダウンロードに成功しました。");
        finalSender.sendMessage("依存関係を読み込み中...");
        PluginDescriptionFile description;
        try
        {
            description = PluginUtil.loadDescription(new File("plugins/" + fileName));
        }
        catch (FileNotFoundException e)
        {
            finalSender.sendMessage(ChatColor.RED + "E: ファイルが見つかりませんでした。");
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            return false;
        }
        catch (IOException | InvalidDescriptionException e)
        {
            finalSender.sendMessage(ChatColor.RED + "E: plugin.ymlを読み込みませんでした。");
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            return false;
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

            System.out.println(dependUrl);
            boolean dependResolve = Installer.install(null, dependUrl);
            if (!dependResolve)
                failedResolve.add(dependency);
            else
            {
                finalSender.sendMessage(ChatColor.GREEN + "+ " + dependUrl.substring(dependUrl.lastIndexOf("/") + 1));
                add++;
            }

        }

        if (sender.equals(dummySender()) && failedResolve.size() > 0)
            return false;

        if (failedResolve.size() > 0)
        {
            finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
            finalSender.sendMessage(ChatColor.YELLOW + "W: " + description.getFullName() + " を正常にインストールしましたが、以下の依存関係のダウンロードに失敗しました。");
            finalSender.sendMessage(String.join(", ", failedResolve));
            return true;
        }

        finalSender.sendMessage(Messages.getStatusMessage(add, remove, modify));
        finalSender.sendMessage(ChatColor.GREEN + "S: " + description.getFullName() + " を正常にインストールしました。");
        return true;
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
