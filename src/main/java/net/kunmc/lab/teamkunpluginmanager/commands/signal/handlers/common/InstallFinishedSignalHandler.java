package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.common;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallFailedInstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.InstallFinishedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandler;
import org.bukkit.ChatColor;

public class InstallFinishedSignalHandler
{
    private static final String statsFormat =
            ChatColor.GREEN + "%d 追加 " + ChatColor.RED + "%d 削除 " + ChatColor.YELLOW + "%d 変更 " + ChatColor.GRAY + "%d 保留";

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

    private void printResultStatistics(InstallResult<?> result)
    {
        terminal.writeLine(String.format(
                statsFormat,
                result.getInstalledCount(),
                result.getRemovedCount(),
                result.getUpgradedCount(),
                result.getPendingCount()
        ));
    }

    private void onSuccess(InstallResult<?> result)
    {
        printResultStatistics(result);
        terminal.success("インストールが正常に完了しました。");
    }

    private void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        printResultStatistics(result);

        Enum<?> progress = result.getProgress().getCurrentTask();
        Enum<?> reason = result.getReason();
        Enum<?> taskStatus = result.getTaskStatus();

        terminal.error("インストールは " + progress + " 時に " + reason + " により失敗しました(" + taskStatus + ")");
    }
}
