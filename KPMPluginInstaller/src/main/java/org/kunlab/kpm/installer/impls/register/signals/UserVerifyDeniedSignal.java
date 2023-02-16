package org.kunlab.kpm.installer.impls.register.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

/**
 * ユーザがキャンセルをクリックしたことを示すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class UserVerifyDeniedSignal extends Signal
{
    @NotNull
    String userCode;
}
