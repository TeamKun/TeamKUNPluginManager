package net.kunmc.lab.teamkunpluginmanager.utils;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import org.bukkit.ChatColor;

public class Utils
{
    public static final String STATS_FORMAT
            = ChatColor.GREEN + "%d 追加 " + ChatColor.RED + "%d 削除 " + ChatColor.YELLOW + "%d 変更 " + ChatColor.GRAY + "%d 保留";
    public static final String[] SIZE_UNITS = {
            "B",
            "kB",
            "MB",
            "GB",
            "TB",
            "PB",
            "EB",
            "ZB",
            "YB"
    };

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

    public static String roundSizeUnit(long size)
    {
        int unit = 0;
        double dSize = size;

        while (dSize > 1024 && unit < SIZE_UNITS.length - 1)
        {
            dSize /= 1024;
            unit++;
        }

        return String.format("%.2f %s", dSize, SIZE_UNITS[unit]);
    }
}
