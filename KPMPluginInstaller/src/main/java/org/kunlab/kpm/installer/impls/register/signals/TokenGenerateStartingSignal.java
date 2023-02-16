package org.kunlab.kpm.installer.impls.register.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kunlab.kpm.signal.Signal;

/**
 * トークンの生成の開始中であることを示すシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TokenGenerateStartingSignal extends Signal
{
    /**
     * 生成を続けるかどうかを示すフラグです。
     */
    private boolean continueGenerate;

    public TokenGenerateStartingSignal()
    {
        this.continueGenerate = true;
    }
}
