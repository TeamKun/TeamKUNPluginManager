package net.kunmc.lab.kpm.installer.impls.register.signals;

import lombok.Value;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

/**
 * トークンが登録されたことを示すシグナルです。
 */
@Value
public class TokenStoredSignal implements Signal
{
    /**
     * 登録されたトークンです。
     */
    @NotNull
    String token;
}
