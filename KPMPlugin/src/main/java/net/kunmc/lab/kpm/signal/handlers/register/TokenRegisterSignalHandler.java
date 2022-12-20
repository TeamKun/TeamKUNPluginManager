package net.kunmc.lab.kpm.signal.handlers.register;

import net.kunmc.lab.kpm.installer.InstallFailedInstallResult;
import net.kunmc.lab.kpm.installer.InstallResult;
import net.kunmc.lab.kpm.installer.impls.register.RegisterErrorCause;
import net.kunmc.lab.kpm.installer.impls.register.RegisterTasks;
import net.kunmc.lab.kpm.installer.impls.register.signals.TokenStoredSignal;
import net.kunmc.lab.kpm.signal.SignalHandler;
import net.kunmc.lab.kpm.signal.handlers.common.InstallFinishedSignalBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;

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
    protected void onSuccess(InstallResult<?> result)
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

    private boolean handleGeneralErrors(RegisterErrorCause cause)
    {
        switch (cause)
        {
            case GENERATE_CANCELLED:
                this.terminal.error("トークンの生成がキャンセルされました。");
                return true;
            case IO_EXCEPTION_OCCURRED:
                this.terminal.error("トークンの生成中にI/Oエラーが発生しました。");
                return true;
            case VERIFICATION_CODE_REQUEST_FAILED:
                this.terminal.error("認証コードの取得に失敗しました。");
                return true;
            case VERIFICATION_FAILED:
                this.terminal.error("コードの有効化に失敗しました。");
                return true;
            default:
                return false;
        }
    }

    @SignalHandler
    public void onTokenStored(TokenStoredSignal signal)
    {
        this.terminal.success("トークンが登録されました。");
    }
}
