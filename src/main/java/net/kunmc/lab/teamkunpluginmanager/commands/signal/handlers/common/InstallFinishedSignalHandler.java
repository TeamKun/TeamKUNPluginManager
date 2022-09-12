package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.common;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallFailedInstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.InstallFinishedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.utils.Utils;

public class InstallFinishedSignalHandler
{
    private final Terminal terminal;

    public InstallFinishedSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    @SignalHandler
    public void onFinished(InstallFinishedSignal finished)
    {
        if (finished.getResult() instanceof InstallFailedInstallResult)
            onFail((InstallFailedInstallResult<?, ?, ?>) finished.getResult());
        else
            onSuccess(finished.getResult());
    }

    private void onSuccess(InstallResult<?> result)
    {
        Utils.printInstallStatistics(terminal, result);
        terminal.success("操作が正常に完了しました。");
    }

    private void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        Utils.printInstallStatistics(terminal, result);

        Enum<?> progress = result.getProgress().getCurrentTask();
        Enum<?> reason = result.getReason();
        Enum<?> taskStatus = result.getTaskStatus();


        String errorMessage = "操作は %s(%s) 時に %s により失敗しました。";

        terminal.error(String.format(
                errorMessage,
                progress,
                taskStatus,
                reason
        ));
    }
}
