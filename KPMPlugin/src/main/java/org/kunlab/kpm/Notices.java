package org.kunlab.kpm;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.lang.LangProvider;

import java.util.List;

public class Notices
{
    private static long LAST_TOKEN_FETCH = 0;
    private static boolean LAST_TOKEN_FETCH_RESULT = false;  // true if the token is alive.

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
            terminal.info(LangProvider.get("notice.plugin.removable"));
            terminal.info("  " + String.join(" ", autoRemovable));
            terminal.hint(LangProvider.get("notice.plugin.removable.suggest"));
        }

        return isAutoRemovable;
    }

    public static boolean printTokenUnset(KPMRegistry registry, Terminal terminal)
    {
        boolean isTokenUnset = !registry.getTokenStore().isTokenAvailable();

        if (isTokenUnset)
        {
            terminal.warn(LangProvider.get("notice.token.unset"));
            terminal.hint(LangProvider.get("notice.token.unset.suggest"));
        }

        return isTokenUnset;
    }

    public static boolean printTokenDead(KPMRegistry registry, Terminal terminal)
    {
        long currentTime = System.currentTimeMillis();
        boolean isTokenAlive;
        if (currentTime - LAST_TOKEN_FETCH > 1000 * 60 * 15)  // Token alive cache expires in 15 minutes.
        {  // Fetch token
            LAST_TOKEN_FETCH = currentTime;
            isTokenAlive = LAST_TOKEN_FETCH_RESULT = registry.getTokenStore().isTokenAlive();
        }
        else  // Use cached result
            isTokenAlive = LAST_TOKEN_FETCH_RESULT;

        if (!isTokenAlive)
        {
            terminal.warn(LangProvider.get("notice.token.dead"));
            terminal.hint(LangProvider.get("notice.token.dead.suggest"));
        }

        return !isTokenAlive;
    }
}
