package org.kunlab.kpm.installer.impls.register.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

/**
 * ユーザの検証コードの有効期限が切れたことを示すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class VerificationCodeExpiredSignal extends Signal
{
    /**
     * ユーザが入力するべきだった検証コードです。
     */
    @NotNull
    String userCode;
}
