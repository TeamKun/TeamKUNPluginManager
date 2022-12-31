package net.kunmc.lab.kpm.installer.impls.register.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

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
