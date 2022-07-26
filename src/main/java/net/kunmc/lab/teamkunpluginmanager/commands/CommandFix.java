package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandFix extends CommandBase
{
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (!TeamKunPluginManager.getPlugin().getSession().lock())
        {
            terminal.error("TeamKunPluginManagerが多重起動しています。");
            return;
        }

        terminal.info("依存関係ツリーを読み込み中...");
        if (!DependencyTree.isErrors())
        {
            terminal.error("エラーは検出されませんでした。");
            TeamKunPluginManager.getPlugin().getSession().unlock();
            return;
        }

        terminal.info(ChatColor.GREEN + "問題を修復しています...");
        DependencyTree.fix();
        terminal.success("問題の修復に成功しました。");
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
        return "kpm.fix";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("エラーを修復します。" + ChatColor.YELLOW + "エラーメッセージが表示された場合のみ実行してください。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
    }
}
