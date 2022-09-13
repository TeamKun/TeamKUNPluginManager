package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.uninstall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.common.InstallFinishedSignalBase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallFailedInstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall.UnInstallTasks;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.UnInstallErrorCause;
import org.jetbrains.annotations.Nullable;

/**
 * アンインストールが完了したときのシグナルを処理するハンドラーです。
 */
public class UninstallFinishedSignalHandler extends InstallFinishedSignalBase<UnInstallTasks, net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall.UnInstallErrorCause>
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

    private boolean handleGeneralErrors(@Nullable net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall.UnInstallErrorCause cause)
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
    protected void onFail(InstallFailedInstallResult<UnInstallTasks, net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall.UnInstallErrorCause, ?> result)
    {
        if (this.handleGeneralErrors(result.getReason()))
            return;

        if (result.getTaskStatus() instanceof UnInstallErrorCause)
        {
            UnInstallErrorCause cause = (UnInstallErrorCause) result.getTaskStatus();
            if (cause == UnInstallErrorCause.SOME_UNINSTALL_FAILED)
                this.terminal.error("いくつかのプラグインのアンインストールに失敗しました。");
        }

    }

}
