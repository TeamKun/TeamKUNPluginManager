package net.kunmc.lab.teamkunpluginmanager.signal.handlers.register;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.register.signals.TokenStoredSignal;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandler;

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
        terminal.success("トークンが登録されました。");
    }


}
