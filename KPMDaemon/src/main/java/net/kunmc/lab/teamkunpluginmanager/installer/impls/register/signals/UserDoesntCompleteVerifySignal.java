package net.kunmc.lab.teamkunpluginmanager.installer.impls.register.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;
import org.jetbrains.annotations.NotNull;

/**
 * ユーザが検証コードの入力を完了していないことを示すシグナルです。
 */
@Value
public class UserDoesntCompleteVerifySignal implements Signal
{
    /**
     * ユーザが入力するコードです。
     */
    @NotNull
    String userCode;

    /**
     * 認証URLです。
     */
    @NotNull
    String verificationUrl;

    /**
     * コードが期限切れになる時間です。
     */
    long expiresIn;

    /**
     * コードの残り有効期限です。
     */
    long remainTime;
}
