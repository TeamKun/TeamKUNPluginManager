package net.kunmc.lab.teamkunpluginmanager.signal.handlers.uninstall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.Utils;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.uninstall.signals.UninstallReadySignal;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandlingUtils;
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
        this.terminal.writeLine(ChatColor.GREEN + "以下のプラグインは「" + ChatColor.RED + "削除" + ChatColor.GREEN + "」されます。");
        this.terminal.writeLine("  " + uninstallTargets.stream()
                .map(Plugin::getName)
                .sorted()
                .collect(Collectors.joining(" ")));
        Utils.printInstallStatistics(this.terminal, 0, uninstallTargets.size(), 0, 0);
    }


    @SignalHandler
    public void onPluginsEnumerated(UninstallReadySignal signal)
    {
        this.printUninstallInfo(signal.getPlugins());
        signal.setContinueUninstall(SignalHandlingUtils.askContinue(this.terminal));
    }
}
