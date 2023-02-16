package org.kunlab.kpm.signal.handlers.upgrade;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.kunlab.kpm.Utils;
import org.kunlab.kpm.installer.InstallFailedInstallResult;
import org.kunlab.kpm.installer.impls.upgrade.UpgradeErrorCause;
import org.kunlab.kpm.installer.impls.upgrade.signals.InstallFailedSignal;
import org.kunlab.kpm.installer.impls.upgrade.signals.InvalidPluginVersionSignal;
import org.kunlab.kpm.installer.impls.upgrade.signals.PluginNotFoundSignal;
import org.kunlab.kpm.installer.impls.upgrade.signals.ResolveFailedSignal;
import org.kunlab.kpm.installer.impls.upgrade.signals.UpgradeReadySignal;
import org.kunlab.kpm.interfaces.resolver.result.ResolveResult;
import org.kunlab.kpm.resolver.result.AbstractSuccessResult;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.signal.SignalHandlingUtils;
import org.kunlab.kpm.task.tasks.resolve.signals.MultiplePluginResolvedSignal;

import java.util.stream.Collectors;

public class UpgradeSignalHandler
{
    // ignore UpdateQueryRetrievedSignal
    // ignore PluginResolveFailedSignal

    private final Terminal terminal;
    private final boolean isAuto;

    public UpgradeSignalHandler(Terminal terminal, boolean isAuto)
    {
        this.terminal = terminal;
        this.isAuto = isAuto;
    }

    @SignalHandler
    public void onPluginNotFound(PluginNotFoundSignal signal)
    {
        if (signal instanceof ResolveFailedSignal)
            return;  // implemented in #onResolveFailed(ResolveFailedSignal)

        this.terminal.warn("プラグイン %s が見つかりませんでした。", signal.getSpecifiedPluginName());
    }

    @SignalHandler
    public void onInvalidPluginVersion(InvalidPluginVersionSignal signal)
    {
        if (this.isAuto)
            this.logErrorMessage(signal, true);
        else
            this.onInvalidPluginVersionMN(signal);
    }

    private void logErrorMessage(InvalidPluginVersionSignal signal, boolean isAuto)
    {
        UpgradeErrorCause reason = signal.getInvalidReason();
        String pluginName = org.kunlab.kpm.utils.Utils.getPluginString(signal.getPlugin());

        String message;

        switch (reason)
        {
            case PLUGIN_IS_OLDER_OR_EQUAL:
                message = String.format(
                        "プラグイン %s は最新です（取得されたバージョン： %s ）。",
                        pluginName,
                        signal.getPluginVersion()
                );
                break;
            case PLUGIN_EXCLUDED:
                message = String.format("プラグイン %s は除外されています。", pluginName);
                break;
            case PLUGIN_VERSION_NOT_DEFINED:
                message = String.format("プラグイン %s は自動アップグレードに対応していません。", pluginName);
                break;
            default:
                message = String.format("プラグイン %s のアップグレードに失敗しました。", pluginName);
        }

        if (isAuto)
            this.terminal.info(message);
        else
            this.terminal.warn(message);
    }

    private void onInvalidPluginVersionMN(InvalidPluginVersionSignal signal)
    {
        this.logErrorMessage(signal, false);

        this.terminal.info("この警告を無視して強制的にアップグレードできますが、強制的なアップグレードは予期しない問題を引き起こす可能性があります。");
        signal.setExcludePlugin(!SignalHandlingUtils.askContinue(this.terminal));
    }

    @SignalHandler
    public void onMultiResolved(MultiplePluginResolvedSignal signal)
    {
        this.terminal.info("複数のプラグインが解決されたため、自動選択を行います。");

        ResolveResult result = signal.getResults().getResolver().autoPickOnePlugin(signal.getResults());

        if (result instanceof AbstractSuccessResult)
            signal.setSpecifiedResult(result);
    }

    @SignalHandler
    public void onUpgradeReady(UpgradeReadySignal signal)
    {
        this.terminal.infoImplicit("以下のプラグインは「" + ChatColor.YELLOW + "アップグレード" + ChatColor.RESET + "」されます。");

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

        this.terminal.warn(
                "プラグイン(不明) のアップグレードは %s で %s により失敗しました。",
                result.getTaskStatus(),
                result.getReason()
        );
    }
}
