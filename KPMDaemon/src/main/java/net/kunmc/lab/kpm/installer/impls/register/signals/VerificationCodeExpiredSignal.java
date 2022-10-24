package net.kunmc.lab.kpm.installer.impls.register.signals;

import lombok.Value;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

/**
 * ユーザの検証コードの有効期限が切れたことを示すシグナルです。
 */
@Value
public class VerificationCodeExpiredSignal implements Signal
{
    /**
     * ユーザが入力するべきだった検証コードです。
     */
    @NotNull
    String userCode;
}