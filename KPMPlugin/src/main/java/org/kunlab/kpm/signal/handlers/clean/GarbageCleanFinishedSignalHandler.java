package org.kunlab.kpm.signal.handlers.clean;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.installer.InstallFailedInstallResult;
import org.kunlab.kpm.installer.interfaces.InstallResult;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;
import org.kunlab.kpm.signal.handlers.common.InstallFinishedSignalBase;
import org.kunlab.kpm.task.tasks.garbage.clean.GarbageCleanErrorCause;

public class GarbageCleanFinishedSignalHandler extends InstallFinishedSignalBase
{
    private static final MsgArgs INSTALLER_NAME = MsgArgs.of("name", "%%installer.clean%%");

    public GarbageCleanFinishedSignalHandler(Terminal terminal)
    {
        super(terminal);
    }

    @Override
    protected void onSuccess(InstallResult<? extends Enum<?>> result)
    {
        this.terminal.success(LangProvider.get("installer.clean.success", INSTALLER_NAME));
    }

    private void handleGarbageCleanErrors(GarbageCleanErrorCause cause)
    {
        switch (cause)
        {
            case CANCELLED:
                this.terminal.warn(LangProvider.get("installer.clean.errors.cancel", INSTALLER_NAME));
                break;
            case ALL_DELETE_FAILED:
                this.terminal.warn(LangProvider.get("installer.clean.errors.fail", INSTALLER_NAME));
                break;
            case INVALID_INTEGRITY:
                this.terminal.warn(LangProvider.get("installer.clean.errors.invalid_integrity"));
                break;
            case NO_GARBAGE:
                this.terminal.warn(LangProvider.get("installer.clean.errors.no_garbage"));
                break;
        }
    }

    @Override
    protected void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        if (result.getReason() != null && result.getReason() instanceof GarbageCleanErrorCause)
            this.handleGarbageCleanErrors((GarbageCleanErrorCause) result.getReason());
        else
            this.handleOtherError(result, INSTALLER_NAME);
    }
}
