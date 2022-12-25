package net.kunmc.lab.kpm.installer.impls.register.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.signal.Signal;

/**
 * 指定されたトークンが有効であるか、チェック中であることを示すシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TokenCheckingSignal extends Signal
{
}
