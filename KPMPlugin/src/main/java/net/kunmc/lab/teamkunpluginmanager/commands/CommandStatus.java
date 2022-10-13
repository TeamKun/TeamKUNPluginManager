package net.kunmc.lab.teamkunpluginmanager.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.KPMDaemon;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AllArgsConstructor
public class CommandStatus extends CommandBase
{
    private final KPMDaemon daemon;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        terminal.writeLine("===現在の状態===");

        terminal.writeLine(ChatColor.GREEN + "プラグイン数" + ChatColor.WHITE + ": " +
                Bukkit.getPluginManager().getPlugins().length);

        List<String> autoRemovable = this.daemon.getPluginMetaManager().getProvider().getUnusedPlugins();
        if (!autoRemovable.isEmpty())
            terminal.writeLine(
                    ChatColor.BLUE + "以下のプラグインがインストールされていますが、もう必要とされていません:\n" +
                            ChatColor.GREEN + "  " + String.join(" ", autoRemovable) + "\n" +
                            ChatColor.BLUE + "これを削除するには、'/kpm autoremove' を利用してください。"
            );

        if (!this.daemon.getTokenStore().isTokenAvailable())
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
