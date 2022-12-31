package net.kunmc.lab.kpm;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;

import java.util.List;

public class Notices
{
    public static void printAllNotice(KPMRegistry registry, Terminal terminal)
    {
        boolean print = printAutoRemovable(registry, terminal);
        if (print)  // If the notice is printed, print a blank line.
            terminal.writeLine("");
        print = printTokenUnset(registry, terminal);
        if (print)
            terminal.writeLine("");
        print = printTokenDead(registry, terminal);
    }

    public static boolean printAutoRemovable(KPMRegistry registry, Terminal terminal)
    {
        List<String> autoRemovable = registry.getPluginMetaManager().getProvider().getUnusedPlugins();
        boolean isAutoRemovable = !autoRemovable.isEmpty();

        if (isAutoRemovable)
        {
            terminal.info("以下のプラグインがインストールされていますが、もう必要とされていません:");
            terminal.info("  " + String.join(" ", autoRemovable));
            terminal.hint("これを削除するには、'/kpm autoremove' を利用してください。");
        }

        return isAutoRemovable;
    }

    public static boolean printTokenUnset(KPMRegistry registry, Terminal terminal)
    {
        boolean isTokenUnset = !registry.getTokenStore().isTokenAvailable();

        if (isTokenUnset)
        {
            terminal.warn("トークンがセットされていません。");
            terminal.hint("/kpm register でトークンを発行できます。");
        }

        return isTokenUnset;
    }

    public static boolean printTokenDead(KPMRegistry registry, Terminal terminal)
    {
        boolean isTokenDead = !registry.getTokenStore().isTokenAlive();

        if (isTokenDead)
        {
            terminal.warn("指定されているトークンは有効期限が切れています。");
            terminal.hint("/kpm register で新しいトークンを発行できます。");
        }

        return isTokenDead;
    }
}
