package net.kunmc.lab.teamkunpluginmanager.installer.impls.register.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;
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
