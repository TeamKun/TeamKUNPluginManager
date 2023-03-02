package org.kunlab.kpm.installer.impls.register.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

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
