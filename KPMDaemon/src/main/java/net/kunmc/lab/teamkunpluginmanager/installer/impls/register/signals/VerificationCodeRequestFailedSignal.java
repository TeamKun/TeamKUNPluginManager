package net.kunmc.lab.teamkunpluginmanager.installer.impls.register.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;
import org.jetbrains.annotations.NotNull;

/**
 * ユーザ検証コード要求に失敗したときに発行されるシグナルです。
 */
@Value
public class VerificationCodeRequestFailedSignal implements Signal
{
    /**
     * HTTPステータスコードです。
     */
    int httpStatusCode;
    /**
     * エラーメッセージです。
     */
    @NotNull
    String errorMessage;
}
