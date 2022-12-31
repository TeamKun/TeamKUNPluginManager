package net.kunmc.lab.kpm.installer.impls.register.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

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
