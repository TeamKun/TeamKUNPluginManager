package org.kunlab.kpm.installer.impls.register.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

/**
 * ユーザ検証コード要求に失敗したときに発行されるシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class VerificationCodeRequestFailedSignal extends Signal
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
