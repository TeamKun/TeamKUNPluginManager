package org.kunlab.kpm.signal.handlers.common;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.interfaces.installer.signals.PluginModifiedSignal;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.utils.Utils;

/**
 * プラグインの変更のシグナルをハンドルするハンドラです.
 */
public class ModifySignalHandler
{
    private final Terminal terminal;

    public ModifySignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    @SignalHandler
    public void onPluginModify(PluginModifiedSignal signal)
    {
        String pluginName = Utils.getPluginString(signal.getPluginDescription());
        String key = "installer.common.mod." + signal.getModifyType().name().toLowerCase();
        this.terminal.writeLine(LangProvider.get(key, MsgArgs.of("name", pluginName)));
    }
}
