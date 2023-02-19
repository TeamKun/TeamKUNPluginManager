package org.kunlab.kpm.signal.handlers.uninstall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.plugin.Plugin;
import org.kunlab.kpm.Utils;
import org.kunlab.kpm.installer.impls.uninstall.signals.UninstallReadySignal;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.signal.SignalHandlingUtils;

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
        this.terminal.successImplicit(LangProvider.get("installer.operation.remove"));
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
        if (!signal.isAutoConfirm())
            signal.setContinueUninstall(SignalHandlingUtils.askContinue(this.terminal));
    }
}
