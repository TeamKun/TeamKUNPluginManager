package org.kunlab.kpm.signal.handlers.autoremove;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.installer.InstallFailedInstallResult;
import org.kunlab.kpm.installer.impls.autoremove.AutoRemoveErrorCause;
import org.kunlab.kpm.interfaces.installer.InstallResult;
import org.kunlab.kpm.signal.handlers.common.InstallFinishedSignalBase;

public class AutoRemoveFinishedSignalHandler extends InstallFinishedSignalBase
{
    public AutoRemoveFinishedSignalHandler(Terminal terminal)
    {
        super(terminal);
    }

    @Override
    protected void onSuccess(InstallResult<? extends Enum<?>> result)
    {
        this.terminal.success("プラグインの自動削除が完了しました。");
    }

    private void handleAutoRemoveErrors(AutoRemoveErrorCause cause)
    {
        switch (cause)
        {
            case UNINSTALLER_INIT_FAILED:
                this.terminal.warn("アンインストーラーの初期化に失敗しました。");
                break;
            case UNINSTALL_FAILED:
                this.terminal.warn("プラグインのアンインストールに失敗しました。");
                break;
            case NO_AUTO_REMOVABLE_PLUGIN_FOUND:
                this.terminal.warn("自動削除が可能なプラグインが見つかりませんでした。");
                break;
            case CANCELLED:
                this.terminal.warn("自動削除がキャンセルされました。");
                break;
        }
    }

    @Override
    protected void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        if (result.getReason() != null && result.getReason() instanceof AutoRemoveErrorCause)
            this.handleAutoRemoveErrors((AutoRemoveErrorCause) result.getReason());
        else if (result.getException() != null)
            this.terminal.error("プラグインの自動削除中に予期しないエラーが発生しました：%s", result.getException());
        else
            this.terminal.error("プラグインの自動削除に失敗しました。");
    }
}
