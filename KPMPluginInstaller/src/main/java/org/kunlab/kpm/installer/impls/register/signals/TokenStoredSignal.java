package org.kunlab.kpm.installer.impls.register.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

/**
 * トークンが登録されたことを示すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class TokenStoredSignal extends Signal
{
    /**
     * 登録されたトークンです。
     */
    @NotNull
    String token;
}
