package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.Installer;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandInstall extends CommandBase
{
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1, 2)) // TODO: Set max to 1 cuz arg of $-CF-$ will be removed.
            return;

        TeamKunPluginManager kpmInstance = TeamKunPluginManager.getPlugin();

        if (!kpmInstance.isTokenAvailable())
        { // TODO: Set level to warn
            terminal.error("トークンが設定されていません！");
            terminal.info("/kpm register でトークンを発行することができます！");
            kpmInstance.getSession().unlock();
            return;
        }

        if (args.length == 1 && args[0].equals("$-CF$")) // TODO: Remove this and refactor to new Question system.
        {
            terminal.success(ChatColor.GREEN + "実行中のインストールをキャンセルしました。");
            return;
        }

        if (!kpmInstance.getSession().lock())
        {
            terminal.error("TeamKunPluginManagerが多重起動しています。");
            return;
        }

        Runner.runAsync(() -> {
            Installer.install(sender, args[0], false, false, false, false);
            kpmInstance.getSession().unlock();
        });
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.install";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("クエリからプラグインを新規インストールします。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("query", "string")
        };
    }
}
