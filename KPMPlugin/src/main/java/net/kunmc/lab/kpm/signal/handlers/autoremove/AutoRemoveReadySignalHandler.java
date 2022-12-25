package net.kunmc.lab.kpm.signal.handlers.autoremove;

import net.kunmc.lab.kpm.Utils;
import net.kunmc.lab.kpm.installer.impls.autoremove.signals.PluginEnumeratedSignal;
import net.kunmc.lab.kpm.signal.SignalHandler;
import net.kunmc.lab.kpm.signal.SignalHandlingUtils;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
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
