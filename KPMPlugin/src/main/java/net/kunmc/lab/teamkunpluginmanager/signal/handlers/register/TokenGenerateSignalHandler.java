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
        terminal.info("GitHub へ Web ブラウザを用いてログインします。");
        terminal.info("この操作により、 KPM はあなたの GitHub アカウントにアクセスできるようになります。");

        signal.setContinueGenerate(SignalHandlingUtils.askContinue(terminal));
    }

    @SignalHandler
    public void onVerificationCodeRequesting(VerificationCodeRequestingSignal signal)
    {
        terminal.info("サーバに接続しています ...");
    }

    @SignalHandler
    public void onVerificationCodeRequestFailed(VerificationCodeRequestFailedSignal signal)
    {
        terminal.removeProgressbar("codeExpire");

        terminal.error("検証コードの取得に失敗しました：" + signal.getHttpStatusCode() + " " + signal.getErrorMessage());
    }

    @SignalHandler
    public void onVerificationCodeReceived(VerificationCodeReceivedSignal signal)
    {
        String userCode = signal.getUserCode();
        String verificationUrl = signal.getVerificationUrl();
        long expiresInSec = signal.getExpiresIn();
        int expiresInMin = (int) (expiresInSec / 60);

        terminal.writeLine(
                ChatColor.DARK_GREEN + "このリンクからコードを有効化してください：" +
                        ChatColor.BLUE + ChatColor.UNDERLINE + verificationUrl);
        terminal.writeLine(ChatColor.DARK_GREEN + "コード： " + ChatColor.WHITE + userCode);
        terminal.info("I：このコードは " + expiresInMin + " 分で失効します。");

        int expiresInSecInt = (int) expiresInSec;

        if (terminal.isPlayer())
            terminal.showNotification(userCode, "GitHubでこのコードを入力して有効化してください。",
                    expiresInSecInt * 1000
            );

        this.progressbar.setProgressMax(expiresInSecInt);
        this.progressbar.setProgress(expiresInSecInt);
        this.progressbar.show();
    }

    @SignalHandler
    public void onVerificationCodeExpired(VerificationCodeExpiredSignal signal)
    {
        terminal.error("コードが失効しました： " + signal.getUserCode());

        progressbar.hide();
        terminal.removeProgressbar("codeExpire");
    }

    @SignalHandler
    public void onUserDoesntCompleteVerify(UserDoesntCompleteVerifySignal signal)
    {
        progressbar.setProgress((int) signal.getRemainTime());
    }

    @SignalHandler
    public void onUserVerifyDenied(UserVerifyDeniedSignal signal)
    {
        progressbar.hide();
        terminal.removeProgressbar("codeExpire");

        terminal.error("コードの有効化に失敗しました：ユーザがコードの有効化を拒否しました。");
    }

    @SignalHandler
    public void onUserVerifySucceeded(UserVerificationSuccessSignal signal)
    {
        progressbar.hide();
        terminal.removeProgressbar("codeExpire");

        terminal.success("コードの有効化に成功しました。");
    }
}
