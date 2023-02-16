package org.kunlab.kpm.signal.handlers.register;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Progressbar;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;
import org.kunlab.kpm.installer.impls.register.signals.TokenGenerateStartingSignal;
import org.kunlab.kpm.installer.impls.register.signals.UserDoesntCompleteVerifySignal;
import org.kunlab.kpm.installer.impls.register.signals.UserVerificationSuccessSignal;
import org.kunlab.kpm.installer.impls.register.signals.UserVerifyDeniedSignal;
import org.kunlab.kpm.installer.impls.register.signals.VerificationCodeExpiredSignal;
import org.kunlab.kpm.installer.impls.register.signals.VerificationCodeReceivedSignal;
import org.kunlab.kpm.installer.impls.register.signals.VerificationCodeRequestFailedSignal;
import org.kunlab.kpm.installer.impls.register.signals.VerificationCodeRequestingSignal;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.signal.SignalHandlingUtils;

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
            this.progressbar.setPrefix(ChatColor.GREEN + "コード有効期限: ");
        }
        else
            this.progressbar = null;
    }

    @SignalHandler
    public void onTokenGenerateStarting(TokenGenerateStartingSignal signal)
    {
        this.terminal.info("GitHub へ Web ブラウザを用いてログインします。");
        this.terminal.info("この操作により、 KPM はあなたの GitHub アカウントにアクセスできるようになります。");

        signal.setContinueGenerate(SignalHandlingUtils.askContinue(this.terminal));
    }

    @SignalHandler
    public void onVerificationCodeRequesting(VerificationCodeRequestingSignal signal)
    {
        this.terminal.info("サーバに接続しています …");
    }

    @SignalHandler
    public void onVerificationCodeRequestFailed(VerificationCodeRequestFailedSignal signal)
    {
        this.terminal.removeProgressbar("codeExpire");

        this.terminal.error(
                "検証コードの取得に失敗しました：%s %s",
                signal.getHttpStatusCode(),
                signal.getErrorMessage()
        );
    }

    @SignalHandler
    public void onVerificationCodeReceived(VerificationCodeReceivedSignal signal)
    {
        String userCode = signal.getUserCode();
        String verificationUrl = signal.getVerificationUrl();
        long expiresInSec = signal.getExpiresIn();
        int expiresInMin = (int) (expiresInSec / 60);

        this.terminal.successImplicit(
                "このリンクからコードを有効化してください：" + ChatColor.BLUE + ChatColor.UNDERLINE + "%s",
                verificationUrl
        );
        this.terminal.successImplicit("コード：" + ChatColor.WHITE + " %s", userCode);
        this.terminal.info("このコードは %d 分で失効します。", expiresInMin);

        int expiresInSecInt = (int) expiresInSec;

        if (this.terminal.isPlayer())
        {
            this.terminal.showNotification(userCode, "GitHubでこのコードを入力して有効化してください。",
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
        this.terminal.error("コードが失効しました： %s", signal.getUserCode());
        this.terminal.info("コードを再取得するには /kpm register を実行してください。");

        if (!this.terminal.isPlayer())
            return;
        this.progressbar.hide();
        this.terminal.removeProgressbar("codeExpire");
    }

    @SignalHandler
    public void onUserDoesntCompleteVerify(UserDoesntCompleteVerifySignal signal)
    {
        if (this.terminal.isPlayer())
            this.progressbar.setProgress((int) signal.getRemainTime());
    }

    @SignalHandler
    public void onUserVerifyDenied(UserVerifyDeniedSignal signal)
    {
        this.terminal.error("コードの有効化に失敗しました：ユーザがコードの有効化を拒否しました。");

        if (!this.terminal.isPlayer())
            return;

        this.progressbar.hide();
        this.terminal.removeProgressbar("codeExpire");
    }

    @SignalHandler
    public void onUserVerifySucceeded(UserVerificationSuccessSignal signal)
    {
        this.terminal.success("コードの有効化に成功しました。");

        if (!this.terminal.isPlayer())
            return;

        this.progressbar.hide();
        this.terminal.removeProgressbar("codeExpire");
    }
}
