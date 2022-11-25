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

    private final Terminal terminal;
    private final int targetPluginCount;
    private int currentPluginCount = 0;
    private int currentPluginResolvedCount = 0;

    public UpgradeSignalHandler(Terminal terminal, int targetPlugins)
    {
        this.terminal = terminal;
        this.targetPluginCount = targetPlugins;
    }

    private boolean askCancelOrContinue(int currentCount)
    {

        if (currentCount == this.targetPluginCount)  // allow -1
        {
            this.terminal.warn("プラグインのアップグレードを中止します。");
            return false;
        }
        else
        {
            this.terminal.info("この警告を無視してアップデートを続行できます。");
            return SignalHandlingUtils.askContinue(this.terminal);
        }
    }

    @SignalHandler
    public void onPluginNotFound(PluginNotFoundSignal signal)
    {
        this.terminal.warn("プラグイン " + signal.getSpecifiedPluginName() + " が見つかりませんでした。");
        signal.setContinueUpgrade(this.askCancelOrContinue(++this.currentPluginCount));
    }

    @SignalHandler
    public void onResolveFailed(ResolveFailedSignal signal)
    {
        this.terminal.warn("プラグイン " + signal.getPlugin().getName() +
                " の解決は " +
                signal.getErrorCause().name() + " で失敗しました。");
        signal.setContinueUpgrade(this.askCancelOrContinue(++this.currentPluginResolvedCount));
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

        signal.setContinueUpgrade(this.askCancelOrContinue(++this.currentPluginCount));
    }
}
