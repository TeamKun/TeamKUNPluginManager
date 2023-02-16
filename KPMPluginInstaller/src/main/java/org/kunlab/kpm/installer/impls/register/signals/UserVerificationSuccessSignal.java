package org.kunlab.kpm.installer.impls.register.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

/**
 * ユーザ検証に成功したことを示すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class UserVerificationSuccessSignal extends Signal
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
