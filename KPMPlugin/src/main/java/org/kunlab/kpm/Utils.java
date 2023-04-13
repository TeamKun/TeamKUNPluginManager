package org.kunlab.kpm;

import lombok.experimental.UtilityClass;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.installer.interfaces.InstallResult;

@UtilityClass
public class Utils
{
    private static final long SIZE_UNIT_THRESHOLD = 1024;  // 1000byte ではなく 1024byte で切り捨てる
    private static final String[] SIZE_UNITS = {
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

    public static void printInstallStatistics(Terminal terminal, int added, int removed, int modified, int pending)
    {
        terminal.writeLine(LangProvider.get(
                "installer.operation.result",
                MsgArgs.of("add", added)
                        .add("remove", removed)
                        .add("modify", modified)
                        .add("pending", pending)
        ));
    }

    public static void printInstallStatistics(Terminal terminal, InstallResult<? extends Enum<?>> result)
    {
        printInstallStatistics(
                terminal,
                result.getInstalledCount(),
                result.getRemovedCount(),
                result.getUpgradedCount(),
                result.getPendingCount()
        );
    }

    public static String roundSizeUnit(long size)
    {
        int unit = 0;
        double dSize = size;

        while (dSize > SIZE_UNIT_THRESHOLD && unit < SIZE_UNITS.length - 1)
        {
            dSize /= SIZE_UNIT_THRESHOLD;
            unit++;
        }

        return String.format("%.2f %s", dSize, SIZE_UNITS[unit]);
    }

}
