package org.kunlab.kpm.signal.handlers.register;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.installer.InstallFailedInstallResult;
import org.kunlab.kpm.installer.impls.register.RegisterErrorCause;
import org.kunlab.kpm.installer.impls.register.RegisterTasks;
import org.kunlab.kpm.installer.impls.register.signals.TokenCheckingSignal;
import org.kunlab.kpm.installer.impls.register.signals.TokenStoredSignal;
import org.kunlab.kpm.interfaces.installer.InstallResult;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.signal.handlers.common.InstallFinishedSignalBase;

public class TokenRegisterSignalHandler extends InstallFinishedSignalBase
{
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
        this.terminal.success("トークンの登録に成功しました。");
    }

    @Override
    protected void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        if (!(result.getProgress().getCurrentTask() instanceof RegisterTasks))
            return;

        if (result.getException() != null)
        {
            this.terminal.error("トークンの生成中に予期しないエラーが発生しました：%s", result.getException());
            return;
        }

        if (result.getReason() instanceof RegisterErrorCause &&
                this.handleGeneralErrors((RegisterErrorCause) result.getReason()))
            return;

        this.terminal.error("トークンの生成中に予期しないエラーが発生しました。");
    }

    @SignalHandler
    public void onTokenChecking(TokenCheckingSignal signal)
    {
        this.terminal.info("トークンの有効性を確認しています …");
    }

    private boolean handleGeneralErrors(RegisterErrorCause cause)
    {
        switch (cause)
        {
            case INVALID_TOKEN:
                this.terminal.error("トークンが無効です。");
                return true;
            case GENERATE_CANCELLED:
                this.terminal.error("トークンの生成がキャンセルされました。");
                return true;
            case IO_EXCEPTION_OCCURRED:
                this.terminal.error("トークンの生成中にI/Oエラーが発生しました。");
                return true;
            case VERIFICATION_CODE_REQUEST_FAILED:
            case VERIFICATION_FAILED:
                return true;
            default:
                return false;
        }
    }

    @SignalHandler
    public void onTokenStored(TokenStoredSignal signal)
    {
        this.terminal.info("トークンが登録されました。");
    }
}