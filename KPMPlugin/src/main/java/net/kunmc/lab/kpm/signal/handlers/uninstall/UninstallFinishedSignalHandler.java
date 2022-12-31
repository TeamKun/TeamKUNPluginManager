package net.kunmc.lab.kpm.signal.handlers.uninstall;

import net.kunmc.lab.kpm.installer.InstallFailedInstallResult;
import net.kunmc.lab.kpm.installer.impls.uninstall.UnInstallErrorCause;
import net.kunmc.lab.kpm.interfaces.installer.InstallResult;
import net.kunmc.lab.kpm.signal.handlers.common.InstallFinishedSignalBase;
import net.kunmc.lab.kpm.task.tasks.uninstall.UninstallErrorCause;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.jetbrains.annotations.Nullable;

/**
 * アンインストールが完了したときのシグナルを処理するハンドラーです。
 */
public class UninstallFinishedSignalHandler extends InstallFinishedSignalBase
{
    public UninstallFinishedSignalHandler(Terminal terminal)
    {
        super(terminal);
    }

    @Override
    protected void onSuccess(InstallResult<? extends Enum<?>> result)
    {
        this.terminal.success("アンインストールが正常に完了しました。");
    }

    private boolean handleGeneralErrors(@Nullable UnInstallErrorCause cause)
    {
        if (cause == null)
            return false;

        switch (cause)
        {
            case PLUGIN_NOT_FOUND:
                this.terminal.error("指定されたプラグインが見つかりませんでした。");
                return true;
            case PLUGIN_IGNORED:
                this.terminal.error("指定されたプラグインが無視リストに登録されています。");
                return true;
            case PLUGIN_IS_DEPENDENCY:
                this.terminal.error("指定されたプラグインが他のプラグインの依存関係に含まれています。");
                return true;
            case CANCELLED:
                this.terminal.error("アンインストールがキャンセルされました。");
                return true;
        }

        return false;
    }

    @Override
    protected void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        if (result.getReason() instanceof UnInstallErrorCause &&
                this.handleGeneralErrors((UnInstallErrorCause) result.getReason()))
            return;
        if (result.getException() != null)
        {
            this.terminal.error("アンインストール中に予期しないエラーが発生しました：%s", result.getException());
            return;
        }


        if (result.getReason() instanceof UnInstallErrorCause)
        {
            UninstallErrorCause cause = (UninstallErrorCause) result.getTaskStatus();
            if (cause == UninstallErrorCause.SOME_UNINSTALL_FAILED)
                this.terminal.error("いくつかのプラグインのアンインストールに失敗しました。");
        }

    }

}
