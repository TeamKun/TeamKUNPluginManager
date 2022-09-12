package net.kunmc.lab.teamkunpluginmanager.utils;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import org.bukkit.ChatColor;

public class Utils
{
    public static final String STATS_FORMAT
            = ChatColor.GREEN + "%d 追加 " + ChatColor.RED + "%d 削除 " + ChatColor.YELLOW + "%d 変更 " + ChatColor.GRAY + "%d 保留";

    public static void printInstallStatistics(Terminal terminal, InstallResult<?> result)
    {
        terminal.writeLine(String.format(
                STATS_FORMAT,
                result.getInstalledCount(),
                result.getRemovedCount(),
                result.getUpgradedCount(),
                result.getPendingCount()
        ));
    }
}
