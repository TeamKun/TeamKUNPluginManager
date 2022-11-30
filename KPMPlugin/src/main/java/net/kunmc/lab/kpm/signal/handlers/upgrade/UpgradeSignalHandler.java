package net.kunmc.lab.kpm.signal.handlers.upgrade;

import net.kunmc.lab.kpm.Utils;
import net.kunmc.lab.kpm.installer.InstallFailedInstallResult;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.InstallFailedSignal;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.PluginNotFoundSignal;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.ResolveFailedSignal;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.UpgradeReadySignal;
import net.kunmc.lab.kpm.signal.SignalHandler;
import net.kunmc.lab.kpm.signal.SignalHandlingUtils;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.util.stream.Collectors;

public class UpgradeSignalHandler
{
    // ignore UpdateQueryRetrievedSignal
    // ignore PluginResolveFailedSignal

    private final Terminal terminal;

    public UpgradeSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    @SignalHandler
    public void onPluginNotFound(PluginNotFoundSignal signal)
    {
        if (signal instanceof ResolveFailedSignal)
            return;  // implemented in #onResolveFailed(ResolveFailedSignal)

        this.terminal.warn("プラグイン " + signal.getSpecifiedPluginName() + " が見つかりませんでした。");
    }

    @SignalHandler
    public void onUpgradeReady(UpgradeReadySignal signal)
    {
        this.terminal.writeLine(
                ChatColor.GREEN + "以下のプラグインは「" +
                        ChatColor.YELLOW + "アップグレード" +
                        ChatColor.GREEN + "」されます。"
        );

        this.terminal.writeLine("  " + signal.getPlugins().stream()
                .map(element -> {
                    Plugin plugin = element.getPlugin();
                    return ChatColor.GREEN + plugin.getName() + " ("
                            + ChatColor.DARK_GREEN + plugin.getDescription().getVersion() + ChatColor.WHITE + " -> "
                            + ChatColor.YELLOW + element.getResolveResult().getVersion() + ChatColor.GREEN + ")";
                })
                .sorted()
                .collect(Collectors.joining("  " + ChatColor.GREEN)));

        Utils.printInstallStatistics(
                this.terminal,
                0,
                0,
                signal.getPlugins().size(),
                0
        );

        signal.setContinueUpgrade(SignalHandlingUtils.askContinue(this.terminal));
    }

    @SignalHandler
    public void onInstallFailed(InstallFailedSignal signal)
    {
        InstallFailedInstallResult<?, ?, ?> result = (InstallFailedInstallResult<?, ?, ?>) signal.getFailedResult();

        this.terminal.warn("プラグイン(不明) のアップグレードは " +
                result.getTaskStatus() + " で " + result.getReason() +
                "により失敗しました。");
    }
}
