package net.kunmc.lab.kpm.signal.handlers.common;

import net.kunmc.lab.kpm.interfaces.installer.signals.PluginModifiedSignal;
import net.kunmc.lab.kpm.signal.SignalHandler;
import net.kunmc.lab.kpm.utils.Utils;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;

/**
 * プラグインの変更のシグナルをハンドルするハンドラです.
 */
public class ModifySignalHandler
{
    private final Terminal terminal;

    public ModifySignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    @SignalHandler
    public void onPluginModify(PluginModifiedSignal signal)
    {
        String pluginStr = Utils.getPluginString(signal.getPluginDescription());
        String printMessage;

        switch (signal.getModifyType())
        {
            case ADD:
                printMessage = ChatColor.GREEN + "+ " + pluginStr;
                break;
            case REMOVE:
                printMessage = ChatColor.RED + "- " + pluginStr;
                break;
            case UPGRADE:
                printMessage = ChatColor.YELLOW + "* " + pluginStr;
                break;
            default:
                printMessage = ChatColor.GRAY + "?" + pluginStr;
                break;
        }

        this.terminal.writeLine(printMessage);
    }
}
