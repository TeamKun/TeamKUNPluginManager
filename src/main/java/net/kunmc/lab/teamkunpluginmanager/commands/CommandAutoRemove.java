package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall.UninstallArgument;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandAutoRemove extends CommandBase
{
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    { // TODO: Separate frontend and backend
        if (checkPermission(sender, terminal, "kpm.autoremove"))
            sender.sendMessage(ChatColor.RED + "E: 権限がありません！");

        if (!TeamKunPluginManager.getPlugin().getSession().lock())
        {
            sender.sendMessage(ChatColor.RED + "E: TeamKunPluginManagerが多重起動しています。");
            return;
        }

        AtomicInteger removed = new AtomicInteger();
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "依存関係ツリーを読み込み中...");

        List<String> removables = TeamKunPluginManager.getPlugin().getPluginMetaManager()
                .getProvider().getUnusedPlugins();
        if (removables.isEmpty())
        {
            sender.sendMessage(ChatColor.RED + "E: 削除可能なプラグインはありません。");
            sender.sendMessage(Messages.getStatusMessage(0, removed.get(), 0));
            sender.sendMessage(ChatColor.GREEN + "S: 操作が正常に完了しました。");
            TeamKunPluginManager.getPlugin().getSession().unlock();
            return;
        }

        try
        {
            TeamKunPluginManager.getPlugin().getInstallManager().runUninstall(
                    terminal,
                    new UninstallArgument(removables.toArray(new String[0]))
            );
        }
        catch (IOException e)
        {
            sender.sendMessage(ChatColor.RED + "E: プラグインの削除に失敗しました。");
            sender.sendMessage(Messages.getStatusMessage(0, removed.get(), 0));
            sender.sendMessage(ChatColor.GREEN + "S: 操作が正常に完了しました。");
            TeamKunPluginManager.getPlugin().getSession().unlock();
            return;
        }

        TeamKunPluginManager.getPlugin().getSession().unlock();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.autoremove";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("必要がなくなったプラグインを自動で削除します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
    }
}
