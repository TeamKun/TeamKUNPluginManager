package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.utils.Messages;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CommandStatus extends CommandBase
{
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        terminal.writeLine("===現在の状態===");

        String statusError = Messages.getErrorMessage();

        terminal.writeLine(ChatColor.GREEN + "ステータス: " +
                (!statusError.isEmpty() ? ChatColor.RED + "エラー": ChatColor.DARK_GREEN + "正常"));
        terminal.writeLine(Messages.keyValue("プラグイン数", Bukkit.getPluginManager().getPlugins().length));

        File resolve = new File(
                TeamKunPluginManager.getPlugin().getDataFolder(),
                TeamKunPluginManager.getPlugin().getPluginConfig().getString("resolvePath")
        );
        if (resolve.exists())
            terminal.writeLine(Messages.keyValue(
                    "最終アップデート",
                    new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(resolve.lastModified()))
            ));

        if (!statusError.isEmpty())
            terminal.writeLine(statusError);

        String autoRemovable = Messages.getUnInstallableMessage();

        if (!autoRemovable.isEmpty())
            terminal.writeLine(autoRemovable);

        if (!TeamKunPluginManager.getPlugin().isTokenAvailable())
            terminal.writeLine(ChatColor.RED + "トークンがセットされていません！/kpm register でトークンを発行してください。");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.status";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("TeamKUNPluginManagerの状態を表示します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
    }
}
