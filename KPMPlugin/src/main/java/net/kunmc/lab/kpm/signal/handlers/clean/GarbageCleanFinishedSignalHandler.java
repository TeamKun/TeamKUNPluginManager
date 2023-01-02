package net.kunmc.lab.kpm.signal.handlers.clean;

import net.kunmc.lab.kpm.installer.InstallFailedInstallResult;
import net.kunmc.lab.kpm.interfaces.installer.InstallResult;
import net.kunmc.lab.kpm.signal.handlers.common.InstallFinishedSignalBase;
import net.kunmc.lab.kpm.task.tasks.garbage.clean.GarbageCleanErrorCause;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;

public class GarbageCleanFinishedSignalHandler extends InstallFinishedSignalBase
{
    public GarbageCleanFinishedSignalHandler(Terminal terminal)
    {
        super(terminal);
    }

    @Override
    protected void onSuccess(InstallResult<? extends Enum<?>> result)
    {
        this.terminal.success("不要なデータの削除に成功しました。");
    }

    private void handleGarbageCleanErrors(GarbageCleanErrorCause cause)
    {
        switch (cause)
        {
            case CANCELLED:
                this.terminal.warn("不要なデータの削除がキャンセルされました。");
                break;
            case ALL_DELETE_FAILED:
                this.terminal.warn("すべての不要なデータの削除に失敗しました。");
                break;
            case INVALID_INTEGRITY:
                this.terminal.warn("ファイル・システムとの不整合が発生したため、システムが保護されました。");
                break;
            case NO_GARBAGE:
                this.terminal.warn("不要なデータが見つかりませんでした。");
                break;
        }
    }

    @Override
    protected void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        if (result.getReason() != null && result.getReason() instanceof GarbageCleanErrorCause)
            this.handleGarbageCleanErrors((GarbageCleanErrorCause) result.getReason());
        else if (result.getException() != null)
            this.terminal.error("不要なデータの削除中に予期しないエラーが発生しました：%s", result.getException());
        else
            this.terminal.error("不要なデータの削除に失敗しました。");
    }
}
