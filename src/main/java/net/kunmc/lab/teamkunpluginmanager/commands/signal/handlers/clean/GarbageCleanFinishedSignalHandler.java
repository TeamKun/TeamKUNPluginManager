package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.clean;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.common.InstallFinishedSignalBase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallFailedInstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.garbage.clean.GarbageCleanErrorCause;

public class GarbageCleanFinishedSignalHandler extends InstallFinishedSignalBase
{
    public GarbageCleanFinishedSignalHandler(Terminal terminal)
    {
        super(terminal);
    }

    @Override
    protected void onSuccess(InstallResult<?> result)
    {
        this.terminal.success("不要データの削除に成功しました。");
    }

    private void handleGarbageCleanErrors(GarbageCleanErrorCause cause)
    {
        switch (cause)
        {
            case CANCELLED:
                this.terminal.warn("不要データの削除がキャンセルされました。");
                break;
            case ALL_DELETE_FAILED:
                this.terminal.warn("すべての不要データの削除に失敗しました。");
                break;
            case INVALID_INTEGRITY:
                this.terminal.warn("ファイル・システムとの不整合が発生したため、システムが保護されました。");
                break;
            case NO_GARBAGE:
                this.terminal.warn("不要データが見つかりませんでした。");
                break;
        }
    }

    @Override
    protected void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        if (result.getReason() != null && result.getReason() instanceof GarbageCleanErrorCause)
            this.handleGarbageCleanErrors((GarbageCleanErrorCause) result.getReason());
        this.terminal.error("不要データの削除に失敗しました。");
    }
}
