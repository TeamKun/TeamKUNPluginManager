package net.kunmc.lab.kpm.signal.handlers.upgrade;

import net.kunmc.lab.kpm.Utils;
import net.kunmc.lab.kpm.installer.InstallFailedInstallResult;
import net.kunmc.lab.kpm.installer.impls.upgrade.UpgradeErrorCause;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.InstallFailedSignal;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.InvalidPluginVersionSignal;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.PluginNotFoundSignal;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.ResolveFailedSignal;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.UpgradeReadySignal;
import net.kunmc.lab.kpm.installer.task.tasks.resolve.signals.MultiplePluginResolvedSignal;
import net.kunmc.lab.kpm.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.result.SuccessResult;
import net.kunmc.lab.kpm.signal.SignalHandler;
import net.kunmc.lab.kpm.signal.SignalHandlingUtils;
import net.kunmc.lab.kpm.utils.PluginUtil;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

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

        this.terminal.warn("プラグイン " + signal.getSpecifiedPluginName() + " が見つかりませんでした。");
    }

    @SignalHandler
    public void onInvalidPluginVersion(InvalidPluginVersionSignal signal)
    {
        if (this.isAuto)
            this.onInvalidPluginVersionAT(signal);
        else
            this.onInvalidPluginVersionMN(signal);
    }

    private void onInvalidPluginVersionAT(InvalidPluginVersionSignal signal)
    {
        UpgradeErrorCause reason = signal.getInvalidReason();
        String pluginName = PluginUtil.getPluginString(signal.getPlugin());

        switch (reason)
        {
            case PLUGIN_IS_OLDER_OR_EQUAL:
                this.terminal.info("プラグイン " + pluginName + " は最新です。 " +
                        "( 取得バージョン：" + signal.getPluginVersion() + " )");
                break;
            case PLUGIN_EXCLUDED:
                this.terminal.info("プラグイン " + pluginName + " は除外されています。");
                break;
            case PLUGIN_VERSION_NOT_DEFINED:
                this.terminal.info("プラグイン " + pluginName + " は自動アップグレードに対応していません。");
                break;
        }
    }

    private void onInvalidPluginVersionMN(InvalidPluginVersionSignal signal)
    {
        UpgradeErrorCause reason = signal.getInvalidReason();
        String pluginName = PluginUtil.getPluginString(signal.getPlugin());

        switch (reason)
        {
            case PLUGIN_IS_OLDER_OR_EQUAL:
                this.terminal.warn("プラグイン " + pluginName + " は最新です。 " +
                        "( 取得バージョン：" + signal.getPluginVersion() + ")");
                break;
            case PLUGIN_EXCLUDED:
                this.terminal.warn("プラグイン " + pluginName + " は除外されています。");
                break;
            case PLUGIN_VERSION_NOT_DEFINED:
                this.terminal.warn("プラグイン " + pluginName + " のバージョンを読み取れませんでした。");
                break;
        }

        this.terminal.info("この警告を無視して強制的にアップグレードできますが、" +
                "強制的なアップグレードは予期しない問題を引き起こす可能性があります。");
        signal.setExcludePlugin(!SignalHandlingUtils.askContinue(this.terminal));
    }

    @SignalHandler
    public void onMultiResolved(MultiplePluginResolvedSignal signal)
    {
        this.terminal.info("複数のプラグインが解決されたため, 自動選択を行います。");

        ResolveResult result = signal.getResults().getResolver().autoPickOnePlugin(signal.getResults());

        if (result instanceof SuccessResult)
            signal.setSpecifiedResult(result);
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
