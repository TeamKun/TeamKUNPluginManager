package net.kunmc.lab.teamkunpluginmanager.install;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.utils.GitHubURLBuilder;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import net.kunmc.lab.teamkunpluginmanager.utils.URLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicReference;

public class Installer
{
    /**
     * URlからぶちこむ！
     * @param url URL!!!
     */
    public static void installFromURLAsync(CommandSender sender, String url)
    {
        AtomicReference<String> atomicURL = new AtomicReference<>(url);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!UrlValidator.getInstance().isValid(atomicURL.get()))
                {
                    if (StringUtils.split(atomicURL.get(), "/").length == 2)
                        atomicURL.set(atomicURL.get() + "https://github.com/");
                    if (KnownPlugins.isKnown(atomicURL.get()))
                        atomicURL.set(atomicURL.get() + KnownPlugins.getKnown(atomicURL.get()).url);
                }

                atomicURL.set(GitHubURLBuilder.urlValidate(atomicURL.get())); //GitHubのURLを正規化

                sender.sendMessage(ChatColor.GREEN + "ファイルのダウンロード中...");

                String fileName = URLUtils.downloadFile(atomicURL.get());
                if (fileName.equals(""))
                {
                    sender.sendMessage(ChatColor.RED + "E: ファイルのダウンロードに失敗しました。");
                    sender.sendMessage(Messages.getStatusMessage(0, 0, 0));
                    return;
                }

                sender.sendMessage(Messages.getModifyMessage(Messages.ModifyType.ADD, fileName));
            }
        }.runTaskAsynchronously(TeamKunPluginManager.plugin);
    }
}
