package net.kunmc.lab.kpm.installer.impls.register.signals;

import lombok.Value;
import net.kunmc.lab.kpm.installer.impls.register.pojos.UserVerificationCodeResponse;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

/**
 * ユーザ検証コードを受信したことを示すシグナルです。
 */
@Value
public class VerificationCodeReceivedSignal implements Signal
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
