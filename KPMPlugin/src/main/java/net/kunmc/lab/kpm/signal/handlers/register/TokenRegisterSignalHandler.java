package net.kunmc.lab.kpm.signal.handlers.register;

import net.kunmc.lab.kpm.installer.impls.register.signals.TokenStoredSignal;
import net.kunmc.lab.kpm.signal.SignalHandler;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;

public class TokenRegisterSignalHandler
{
    private final Terminal terminal;

    public TokenRegisterSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    @SignalHandler
    public void onTokenStored(TokenStoredSignal signal)
    {
        this.terminal.success("トークンが登録されました。");
    }


}
