package net.kunmc.lab.teamkunpluginmanager.signal.handlers.autoremove;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.Utils;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.autoremove.signals.PluginEnumeratedSignal;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandlingUtils;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class AutoRemoveReadySignalHandler
{
    private final Terminal terminal;

    public AutoRemoveReadySignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    private void printUninstallInfo(List<String> uninstallTargets)
    {
        this.terminal.writeLine(
                ChatColor.GREEN + "以下のプラグインが自動でインストールされましたが、もう必要とされていません：");
        this.terminal.writeLine(
                ChatColor.GREEN + "以下のプラグインは「"
                        + ChatColor.RED + "削除" + ChatColor.GREEN + "」されます。");
        this.terminal.writeLine("  " + uninstallTargets.stream()
                .sorted()
                .collect(Collectors.joining(" ")));
        Utils.printInstallStatistics(this.terminal, 0, uninstallTargets.size(), 0, 0);
    }


    @SignalHandler
    public void onPluginEnumerated(PluginEnumeratedSignal signal)
    {
        this.printUninstallInfo(signal.getTargetPlugins());
        signal.setCancel(!SignalHandlingUtils.askContinue(this.terminal));
    }
}
