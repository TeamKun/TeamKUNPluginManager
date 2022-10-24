package net.kunmc.lab.kpm.installer.impls.register.signals;

import lombok.Value;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

/**
 * ユーザ検証に成功したことを示すシグナルです。
 */
@Value
public class UserVerificationSuccessSignal implements Signal
{
    /**
     * アクセストークンです。
     */
    @NotNull
    String accessToken;

    /**
     * トークンのタイプです。(例：<code>Bearer</code>)
     */
    @NotNull
    String tokenType;

    /**
     * トークンのスコープです。
     */
    @NotNull
    String scope;
}