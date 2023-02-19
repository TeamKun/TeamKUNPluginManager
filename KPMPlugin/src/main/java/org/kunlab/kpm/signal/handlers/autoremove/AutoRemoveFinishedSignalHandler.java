package org.kunlab.kpm.signal.handlers.autoremove;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.installer.InstallFailedInstallResult;
import org.kunlab.kpm.installer.impls.autoremove.AutoRemoveErrorCause;
import org.kunlab.kpm.interfaces.installer.InstallResult;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;
import org.kunlab.kpm.signal.handlers.common.InstallFinishedSignalBase;

public class AutoRemoveFinishedSignalHandler extends InstallFinishedSignalBase
{
    private static final MsgArgs INSTALLER_NAME = MsgArgs.of("name", "%%installer.autoremove%%");

    public AutoRemoveFinishedSignalHandler(Terminal terminal)
    {
        super(terminal);
    }

    @Override
    protected void onSuccess(InstallResult<? extends Enum<?>> result)
    {
        this.terminal.success(LangProvider.get("installer.autoremove.success", INSTALLER_NAME));
    }

    private void handleAutoRemoveErrors(AutoRemoveErrorCause cause)
    {
        String key;
        switch (cause)
        {
            case UNINSTALLER_INIT_FAILED:
                key = "installer.autoremove.errors.uninstallerInitFailed";
                break;
            case UNINSTALL_FAILED:
                key = "installer.autoremove.errors.uninstallFailed";
                break;
            case NO_AUTO_REMOVABLE_PLUGIN_FOUND:
                key = "installer.autoremove.errors.noAutoRemovablePluginFound";
                break;
            case CANCELLED:
                key = "installer.autoremove.errors.cancel";
                this.terminal.warn(LangProvider.get(key, INSTALLER_NAME));
                return;
            default:
                key = null;
        }

        if (key != null)
            this.terminal.warn(LangProvider.get(key));
    }

    @Override
    protected void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        if (result.getReason() != null && result.getReason() instanceof AutoRemoveErrorCause)
            this.handleAutoRemoveErrors((AutoRemoveErrorCause) result.getReason());
        else
            this.handleOtherError(result, INSTALLER_NAME);
    }
}
