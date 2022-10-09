package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.uninstall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.commands.signal.SignalHandlingUtils;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall.signals.UninstallReadySignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.stream.Collectors;

public class UninstallReadySignalHandler
{
    private final Terminal terminal;

    public UninstallReadySignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    private void printUninstallInfo(List<Plugin> uninstallTargets)
    {
        terminal.writeLine(ChatColor.GREEN + "以下のプラグインは「" + ChatColor.RED + "削除" + ChatColor.GREEN + "」されます。");
        terminal.writeLine("  " + uninstallTargets.stream()
                .map(Plugin::getName)
                .sorted()
                .collect(Collectors.joining(" ")));
        Utils.printInstallStatistics(terminal, 0, uninstallTargets.size(), 0, 0);
    }


    @SignalHandler
    public void onPluginsEnumerated(UninstallReadySignal signal)
    {
        printUninstallInfo(signal.getPlugins());
        signal.setContinueUninstall(SignalHandlingUtils.askContinue(terminal));
    }
}
