package net.kunmc.lab.kpm.installer.impls.register.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

/**
 * ユーザが検証コードの入力を完了していないことを示すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class UserDoesntCompleteVerifySignal extends Signal
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
