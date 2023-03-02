package org.kunlab.kpm.installer.impls.register.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.impls.register.pojos.UserVerificationCodeResponse;
import org.kunlab.kpm.signal.Signal;

/**
 * ユーザ検証コードを受信したことを示すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class VerificationCodeReceivedSignal extends Signal
{
    /**
     * ユーザが入力する検証コードです。
     */
    @NotNull
    String userCode;

    /**
     * ユーザが検証コードを入力するURLです。
     */
    @NotNull
    String verificationUrl;

    /**
     * 認証コードの有効期限(秒)です。
     */
    long expiresIn;

    /**
     * 認証コードの有効期限のUNIX時間です。
     */
    long expiresAt;

    public VerificationCodeReceivedSignal(@NotNull UserVerificationCodeResponse apiResponse)
    {
        this.userCode = apiResponse.getUserCode();
        this.verificationUrl = apiResponse.getVerificationUrl();
        this.expiresIn = apiResponse.getExpiresIn();  // Unit: seconds
        this.expiresAt = System.currentTimeMillis() + (apiResponse.getExpiresIn() * 1000);
    }
}
