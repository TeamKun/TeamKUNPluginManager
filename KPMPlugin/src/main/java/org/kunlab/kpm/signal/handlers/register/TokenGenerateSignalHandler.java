package org.kunlab.kpm.signal.handlers.register;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Progressbar;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.installer.impls.register.signals.TokenGenerateStartingSignal;
import org.kunlab.kpm.installer.impls.register.signals.UserDoesntCompleteVerifySignal;
import org.kunlab.kpm.installer.impls.register.signals.UserVerificationSuccessSignal;
import org.kunlab.kpm.installer.impls.register.signals.UserVerifyDeniedSignal;
import org.kunlab.kpm.installer.impls.register.signals.VerificationCodeExpiredSignal;
import org.kunlab.kpm.installer.impls.register.signals.VerificationCodeReceivedSignal;
import org.kunlab.kpm.installer.impls.register.signals.VerificationCodeRequestFailedSignal;
import org.kunlab.kpm.installer.impls.register.signals.VerificationCodeRequestingSignal;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.signal.SignalHandlingUtils;

import java.util.concurrent.TimeUnit;

public class TokenGenerateSignalHandler
{
    private final Progressbar progressbar;
    private final Terminal terminal;

    public TokenGenerateSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
        if (terminal.isPlayer())
        {
            this.progressbar = terminal.createProgressbar("codeExpire");
            this.progressbar.setPrefix(LangProvider.get("tasks.gen_token.code_alive"));
        }
        else
            this.progressbar = null;
    }

    @SignalHandler
    public void onTokenGenerateStarting(TokenGenerateStartingSignal signal)
    {
        this.terminal.info(LangProvider.get("tasks.gen_token.start.1"));
        this.terminal.info(LangProvider.get("tasks.gen_token.start.2"));
        signal.setContinueGenerate(SignalHandlingUtils.askContinue(this.terminal));
    }

    @SignalHandler
    public void onVerificationCodeRequesting(VerificationCodeRequestingSignal signal)
    {
        this.terminal.info(LangProvider.get("tasks.gen_token.request.requesting"));
    }

    @SignalHandler
    public void onVerificationCodeRequestFailed(VerificationCodeRequestFailedSignal signal)
    {
        this.terminal.removeProgressbar("codeExpire");

        this.terminal.error(LangProvider.get(
                "tasks.gen_token.request.fail",
                MsgArgs.of("httpStatus", signal.getHttpStatusCode())
                        .add("message", signal.getErrorMessage())
        ));
    }

    @SignalHandler
    public void onVerificationCodeReceived(VerificationCodeReceivedSignal signal)
    {
        String userCode = signal.getUserCode();
        String verificationUrl = signal.getVerificationUrl();
        long expiresInSec = signal.getExpiresIn();
        int expiresInMin = Math.toIntExact(TimeUnit.SECONDS.toMinutes(expiresInSec));

        this.terminal.successImplicit(LangProvider.get(
                "tasks.gen_token.verify.verify_link",
                MsgArgs.of("link", verificationUrl)
        ));
        this.terminal.successImplicit(LangProvider.get(
                "tasks.gen_token.verify.code",
                MsgArgs.of("code", userCode)
        ));
        this.terminal.info(LangProvider.get(
                "tasks.gen_token.verify.expiresIn",
                MsgArgs.of("minutes", expiresInMin)
        ));

        int expiresInSecInt = Math.toIntExact(expiresInSec);

        if (this.terminal.isPlayer())
        {
            this.terminal.showNotification(userCode, LangProvider.get("tasks.gen_token.verify.title"),
                    expiresInSecInt * 1000
            );

            this.progressbar.setProgressMax(expiresInSecInt);
            this.progressbar.setProgress(expiresInSecInt);
            this.progressbar.show();
        }
    }

    @SignalHandler
    public void onVerificationCodeExpired(VerificationCodeExpiredSignal signal)
    {
        this.terminal.error(LangProvider.get(
                "tasks.gen_token.verify.fail.expire",
                MsgArgs.of("code", signal.getUserCode())
        ));
        this.terminal.hint(LangProvider.get(
                "tasks.gen_token.verify.fail.expire.hint",
                MsgArgs.of("command", "/kpm register")
        ));

        if (!this.terminal.isPlayer())
            return;
        this.progressbar.hide();
        this.terminal.removeProgressbar("codeExpire");
    }

    @SignalHandler
    public void onUserDoesntCompleteVerify(UserDoesntCompleteVerifySignal signal)
    {
        if (this.terminal.isPlayer())
            this.progressbar.setProgress(Math.toIntExact(signal.getRemainTime()));
    }

    @SignalHandler
    public void onUserVerifyDenied(UserVerifyDeniedSignal signal)
    {
        this.terminal.error(LangProvider.get(
                "tasks.gen_token.verify.fail.denied",
                MsgArgs.of("reason", "%%tasks.gen_token.verify.fail.denied")
        ));

        if (!this.terminal.isPlayer())
            return;

        this.progressbar.hide();
        this.terminal.removeProgressbar("codeExpire");
    }

    @SignalHandler
    public void onUserVerifySucceeded(UserVerificationSuccessSignal signal)
    {
        this.terminal.success(LangProvider.get("tasks.gen_token.verify.success"));

        if (!this.terminal.isPlayer())
            return;

        this.progressbar.hide();
        this.terminal.removeProgressbar("codeExpire");
    }
}
