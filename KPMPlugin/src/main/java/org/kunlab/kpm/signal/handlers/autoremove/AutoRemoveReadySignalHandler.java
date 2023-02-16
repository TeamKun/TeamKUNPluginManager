package org.kunlab.kpm.signal.handlers.autoremove;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;
import org.kunlab.kpm.Utils;
import org.kunlab.kpm.installer.impls.autoremove.signals.PluginEnumeratedSignal;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.signal.SignalHandlingUtils;

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
        this.terminal.successImplicit("以下のプラグインが自動でインストールされましたが、もう必要とされていません：");
        this.terminal.successImplicit("以下のプラグインは「" + ChatColor.RED + "削除" + ChatColor.RESET + "」されます。");
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
