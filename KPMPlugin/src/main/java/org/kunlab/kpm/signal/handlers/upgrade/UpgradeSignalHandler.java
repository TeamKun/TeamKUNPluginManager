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
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;
import org.kunlab.kpm.resolver.interfaces.result.ResolveResult;
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

        this.terminal.warn(LangProvider.get(
                "installer.upgrade.not_found",
                MsgArgs.of("name", signal.getSpecifiedPluginName())
        ));
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
                message = LangProvider.get(
                        "installer.upgrade.errors.older_or_equal",
                        MsgArgs.of("name", pluginName)
                                .add("version", signal.getPluginVersion())
                );
                break;
            case PLUGIN_EXCLUDED:
                message = LangProvider.get(
                        "installer.common.checkenv.excluded",
                        MsgArgs.of("name", pluginName)
                );
                break;
            case PLUGIN_VERSION_NOT_DEFINED:
                message = LangProvider.get(
                        "installer.upgrade.errors.version_not_def",
                        MsgArgs.of("name", pluginName)
                );
                break;
            default:
                message = LangProvider.get(
                        "installer.upgrade.errors.unknown",
                        MsgArgs.of("name", pluginName)
                );
        }

        if (isAuto)
            this.terminal.info(message);
        else
            this.terminal.warn(message);
    }

    private void onInvalidPluginVersionMN(InvalidPluginVersionSignal signal)
    {
        this.logErrorMessage(signal, false);

        this.terminal.info(LangProvider.get("installer.upgrade.can_force"));
        signal.setExcludePlugin(!SignalHandlingUtils.askContinue(this.terminal));
    }

    @SignalHandler
    public void onMultiResolved(MultiplePluginResolvedSignal signal)
    {
        this.terminal.info(LangProvider.get("installer.upgrade.resolve.auto_pick"));
        ResolveResult result = signal.getResults().getResolver().autoPickOnePlugin(signal.getResults());

        if (result instanceof AbstractSuccessResult)
            signal.setSpecifiedResult(result);
    }

    @SignalHandler
    public void onUpgradeReady(UpgradeReadySignal signal)
    {
        this.terminal.infoImplicit(LangProvider.get("installer.upgrade.ready.modify"));

        this.terminal.writeLine("  " + signal.getPlugins().stream()
                .map(element -> {
                    Plugin plugin = element.getPlugin();
                    return LangProvider.get(
                            "installer.upgrade.ready.plugins",
                            MsgArgs.of("name", plugin.getName())
                                    .add("version", plugin.getDescription().getVersion())
                                    .add("newVersion", element.getResolveResult().getVersion())
                    );
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
        this.terminal.warn(LangProvider.get(
                "installer.upgrade.fail_unknown",
                MsgArgs.of("status", result.getTaskStatus())
                        .add("error", result.getReason())
        ));
    }
}
