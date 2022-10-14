package net.kunmc.lab.teamkunpluginmanager.installer.impls.register.pojos;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * ユーザが検証コードを入力し、トークンの取得が完了したことを示すレスポンスです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class VerificationSubmitPollingResponse
{
    /**
     * アクセストークンです。
     */
    @NotNull
    @SerializedName("access_token")
    String accessToken;

    /**
     * トークンの種類です。
     */
    @NotNull
    @SerializedName("token_type")
    String tokenType;

    /**
     * トークンのスコープです。
     */
    @NotNull
    @SerializedName("scope")
    String scope;
}
