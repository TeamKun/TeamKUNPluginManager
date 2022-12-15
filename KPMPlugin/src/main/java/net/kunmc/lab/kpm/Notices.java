package net.kunmc.lab.kpm;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;

import java.util.List;

public class Notices
{
    public static void printAllNotice(KPMDaemon daemon, Terminal terminal)
    {
        boolean print = printAutoRemovable(daemon, terminal);
        if (print)  // If there is a notice, print a blank line
            terminal.writeLine("");
        print = printTokenUnset(daemon, terminal);
    }

    public static boolean printAutoRemovable(KPMDaemon daemon, Terminal terminal)
    {
        List<String> autoRemovable = daemon.getPluginMetaManager().getProvider().getUnusedPlugins();
        boolean isAutoRemovable = !autoRemovable.isEmpty();

        if (isAutoRemovable)
        {
            terminal.info("以下のプラグインがインストールされていますが、もう必要とされていません:");
            terminal.info("  " + String.join(" ", autoRemovable));
            terminal.hint("これを削除するには、'/kpm autoremove' を利用してください。");
        }

        return isAutoRemovable;
    }

    public static boolean printTokenUnset(KPMDaemon daemon, Terminal terminal)
    {
        boolean isTokenUnset = !daemon.getTokenStore().isTokenAvailable();

        if (isTokenUnset)
        {
            terminal.warn("トークンがセットされていません。");
            terminal.hint("/kpm register でトークンを発行できます。");
        }

        return isTokenUnset;
    }
}
