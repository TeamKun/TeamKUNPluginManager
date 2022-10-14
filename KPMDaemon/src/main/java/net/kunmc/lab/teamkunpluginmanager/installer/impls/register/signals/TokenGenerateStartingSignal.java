package net.kunmc.lab.teamkunpluginmanager.installer.impls.register.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;

/**
 * トークンの生成の開始中であることを示すシグナルです。
 */
@Data
public class TokenGenerateStartingSignal implements Signal
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
