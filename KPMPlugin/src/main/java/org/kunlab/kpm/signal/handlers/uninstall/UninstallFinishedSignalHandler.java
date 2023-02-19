package org.kunlab.kpm.signal.handlers.uninstall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.installer.InstallFailedInstallResult;
import org.kunlab.kpm.installer.impls.uninstall.UnInstallErrorCause;
import org.kunlab.kpm.interfaces.installer.InstallResult;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;
import org.kunlab.kpm.signal.handlers.common.InstallFinishedSignalBase;
import org.kunlab.kpm.task.tasks.uninstall.UninstallErrorCause;

/**
 * アンインストールが完了したときのシグナルを処理するハンドラーです。
 */
public class UninstallFinishedSignalHandler extends InstallFinishedSignalBase
{
    private static final MsgArgs INSTALLER_NAME = MsgArgs.of("name", "%%installer.uninstall%%");

    public UninstallFinishedSignalHandler(Terminal terminal)
    {
        super(terminal);
    }

    @Override
    protected void onSuccess(InstallResult<? extends Enum<?>> result)
    {
        this.terminal.success(LangProvider.get(
                "installer.uninstall.success",
                INSTALLER_NAME
        ));
    }

    private boolean handleGeneralErrors(@Nullable UnInstallErrorCause cause)
    {
        if (cause == null)
            return false;

        String key;
        String name = null;  // Alternative of INSTALLER_NAME
        boolean named = false;
        switch (cause)
        {
            case PLUGIN_NOT_FOUND:
                key = "installer.uninstall.errors.not_found";
                name = "%%general.plugin.specified_plugin%%";
                named = true;
                break;
            case PLUGIN_IGNORED:
                key = "installer.uninstall.errors.excluded";
                break;
            case PLUGIN_IS_DEPENDENCY:
                key = "installer.uninstall.errors.dependency";
                break;
            case CANCELLED:
                key = "installer.uninstall.errors.cancel";
                named = true;
                break;
            default:
                return false;
        }

        if (named)
        {
            if (name == null)
                this.terminal.error(LangProvider.get(key, INSTALLER_NAME));
            else
                this.terminal.error(LangProvider.get(key, MsgArgs.of("name", name)));
        }
        else
            this.terminal.error(LangProvider.get(key));

        return true;
    }

    @Override
    protected void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        if (result.getReason() instanceof UnInstallErrorCause &&
                this.handleGeneralErrors((UnInstallErrorCause) result.getReason()))
            return;

        if (result.getReason() instanceof UnInstallErrorCause)
        {
            UninstallErrorCause cause = (UninstallErrorCause) result.getTaskStatus();
            if (cause == UninstallErrorCause.SOME_UNINSTALL_FAILED)
                this.terminal.error(LangProvider.get("tasks.uninstall.errors.some_plugin"));
        }
        else
            this.handleOtherError(result, INSTALLER_NAME);
    }

}
