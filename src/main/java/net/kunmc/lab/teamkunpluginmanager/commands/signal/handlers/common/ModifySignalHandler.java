package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.common;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.PluginModifiedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
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
        String pluginStr = PluginUtil.getPluginString(signal.getPluginDescription());
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

        terminal.writeLine(printMessage);
    }
}
