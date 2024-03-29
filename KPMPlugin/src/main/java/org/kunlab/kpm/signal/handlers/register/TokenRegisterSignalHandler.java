package org.kunlab.kpm.signal.handlers.register;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.installer.InstallFailedInstallResult;
import org.kunlab.kpm.installer.impls.register.RegisterErrorCause;
import org.kunlab.kpm.installer.impls.register.RegisterTasks;
import org.kunlab.kpm.installer.impls.register.signals.TokenCheckingSignal;
import org.kunlab.kpm.installer.impls.register.signals.TokenStoredSignal;
import org.kunlab.kpm.installer.interfaces.InstallResult;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.signal.handlers.common.InstallFinishedSignalBase;

public class TokenRegisterSignalHandler extends InstallFinishedSignalBase
{

    private static final MsgArgs INSTALLER_NAME = MsgArgs.of("name", "%%installer.register%%");

    private final Terminal terminal;

    public TokenRegisterSignalHandler(Terminal terminal)
    {
        super(terminal);
        this.terminal = terminal;
        this.setPrintResult(false);
    }

    @Override
    protected void onSuccess(InstallResult<? extends Enum<?>> result)
    {
        if (this.terminal.isPlayer())
            this.terminal.removeProgressbar("codeExpire");

        this.terminal.success("トークンの登録に成功しました。");
    }

    @Override
    protected void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        if (this.terminal.isPlayer())
            this.terminal.removeProgressbar("codeExpire");

        if (!(result.getProgress().getCurrentTask() instanceof RegisterTasks))
            return;

        if (result.getReason() instanceof RegisterErrorCause &&
                this.handleGeneralErrors((RegisterErrorCause) result.getReason()))
            return;

        this.handleOtherError(result, INSTALLER_NAME);
    }

    @SignalHandler
    public void onTokenChecking(TokenCheckingSignal signal)
    {
        this.terminal.info(LangProvider.get("installer.register.token.checking"));
    }

    private boolean handleGeneralErrors(RegisterErrorCause cause)
    {
        String key;
        boolean named = false;
        switch (cause)
        {
            case INVALID_TOKEN:
                key = "installer.register.errors.invalid_token";
                break;
            case GENERATE_CANCELLED:
                key = "installer.register.errors.cancel";
                named = true;
                break;
            case IO_EXCEPTION_OCCURRED:
                key = "installer.register.errors.io_exception";
                named = true;
                break;
            case VERIFICATION_CODE_REQUEST_FAILED:
            case VERIFICATION_FAILED:
                return true;
            default:
                return false;
        }

        if (named)
            this.terminal.error(LangProvider.get(key, INSTALLER_NAME));
        else
            this.terminal.error(LangProvider.get(key));

        return true;
    }

    @SignalHandler
    public void onTokenStored(TokenStoredSignal signal)
    {
        this.terminal.info("トークンが登録されました。");
    }
}
