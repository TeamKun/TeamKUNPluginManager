package net.kunmc.lab.teamkunpluginmanager.installer.impls.register.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;
import org.jetbrains.annotations.NotNull;

/**
 * ユーザがキャンセルをクリックしたことを示すシグナルです。
 */
@Value
public class UserVerifyDeniedSignal implements Signal
{
    @NotNull
    String userCode;
}
