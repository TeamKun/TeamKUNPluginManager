package net.kunmc.lab.teamkunpluginmanager.signal.handlers.register;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Progressbar;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.register.signals.TokenGenerateStartingSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.register.signals.UserDoesntCompleteVerifySignal;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.register.signals.UserVerificationSuccessSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.register.signals.UserVerifyDeniedSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.register.signals.VerificationCodeExpiredSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.register.signals.VerificationCodeReceivedSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.register.signals.VerificationCodeRequestFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.register.signals.VerificationCodeRequestingSignal;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandlingUtils;
import org.bukkit.ChatColor;

public class TokenGenerateSignalHandler
{
    private final Progressbar progressbar;
    private final Terminal terminal;

    public TokenGenerateSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
        this.progressbar = terminal.createProgressbar("codeExpire");
        this.progressbar.setPrefix(ChatColor.GREEN + "コード有効期限: ");
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
        this.terminal.info("サーバに接続しています ...");
    }

    @SignalHandler
    public void onVerificationCodeRequestFailed(VerificationCodeRequestFailedSignal signal)
    {
        this.terminal.removeProgressbar("codeExpire");

        this.terminal.error("検証コードの取得に失敗しました：" + signal.getHttpStatusCode() + " " + signal.getErrorMessage());
    }

    @SignalHandler
    public void onVerificationCodeReceived(VerificationCodeReceivedSignal signal)
    {
        String userCode = signal.getUserCode();
        String verificationUrl = signal.getVerificationUrl();
        long expiresInSec = signal.getExpiresIn();
        int expiresInMin = (int) (expiresInSec / 60);

        this.terminal.writeLine(
                ChatColor.DARK_GREEN + "このリンクからコードを有効化してください：" +
                        ChatColor.BLUE + ChatColor.UNDERLINE + verificationUrl);
        this.terminal.writeLine(ChatColor.DARK_GREEN + "コード： " + ChatColor.WHITE + userCode);
        this.terminal.info("このコードは " + expiresInMin + " 分で失効します。");

        int expiresInSecInt = (int) expiresInSec;

        if (this.terminal.isPlayer())
            this.terminal.showNotification(userCode, "GitHubでこのコードを入力して有効化してください。",
                    expiresInSecInt * 1000
            );

        this.progressbar.setProgressMax(expiresInSecInt);
        this.progressbar.setProgress(expiresInSecInt);
        this.progressbar.show();
    }

    @SignalHandler
    public void onVerificationCodeExpired(VerificationCodeExpiredSignal signal)
    {
        this.terminal.error("コードが失効しました： " + signal.getUserCode());

        this.progressbar.hide();
        this.terminal.removeProgressbar("codeExpire");
    }

    @SignalHandler
    public void onUserDoesntCompleteVerify(UserDoesntCompleteVerifySignal signal)
    {
        this.progressbar.setProgress((int) signal.getRemainTime());
    }

    @SignalHandler
    public void onUserVerifyDenied(UserVerifyDeniedSignal signal)
    {
        this.progressbar.hide();
        this.terminal.removeProgressbar("codeExpire");

        this.terminal.error("コードの有効化に失敗しました：ユーザがコードの有効化を拒否しました。");
    }

    @SignalHandler
    public void onUserVerifySucceeded(UserVerificationSuccessSignal signal)
    {
        this.progressbar.hide();
        this.terminal.removeProgressbar("codeExpire");

        this.terminal.success("コードの有効化に成功しました。");
    }
}
