package net.kunmc.lab.kpm.installer.impls.register.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

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
