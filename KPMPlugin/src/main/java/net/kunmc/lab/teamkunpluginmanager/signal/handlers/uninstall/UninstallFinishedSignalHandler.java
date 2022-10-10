package net.kunmc.lab.teamkunpluginmanager.signal.handlers.uninstall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.installer.InstallFailedInstallResult;
import net.kunmc.lab.teamkunpluginmanager.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.uninstall.UnInstallErrorCause;
import net.kunmc.lab.teamkunpluginmanager.signal.handlers.common.InstallFinishedSignalBase;
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
    protected void onSuccess(InstallResult<?> result)
    {
        this.terminal.success("アンインストールが正常に完了しました。");
    }

    private boolean handleGeneralErrors(@Nullable net.kunmc.lab.teamkunpluginmanager.installer.impls.uninstall.UnInstallErrorCause cause)
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
        }

        return false;
    }

    @Override
    protected void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        if (result.getReason() instanceof net.kunmc.lab.teamkunpluginmanager.installer.impls.uninstall.UnInstallErrorCause &&
                this.handleGeneralErrors((net.kunmc.lab.teamkunpluginmanager.installer.impls.uninstall.UnInstallErrorCause) result.getReason()))
            return;

        if (result.getReason() instanceof UnInstallErrorCause)
        {
            UnInstallErrorCause cause = (UnInstallErrorCause) result.getTaskStatus();
            if (cause == UnInstallErrorCause.SOME_UNINSTALL_FAILED)
                this.terminal.error("いくつかのプラグインのアンインストールに失敗しました。");
        }

    }

}
