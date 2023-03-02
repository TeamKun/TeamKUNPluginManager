package org.kunlab.kpm.installer.impls.register.pojos;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * ユーザ検証コード要求のレスポンスです。
 */
@Value
public class UserVerificationCodeResponse
{
    /**
     * デバイスコードです。
     */
    @NotNull
    @SerializedName("device_code")
    String deviceCode;

    /**
     * ユーザコードです。
     */
    @NotNull
    @SerializedName("user_code")
    String userCode;

    /**
     * 認証URLです。
     */
    @NotNull
    @SerializedName("verification_uri")
    String verificationUrl;

    /**
     * 認証コードの有効期限です。
     */
    @SerializedName("expires_in")
    long expiresIn;

    /**
     * ポーリング間隔です。
     */
    @SerializedName("interval")
    long interval;
}
