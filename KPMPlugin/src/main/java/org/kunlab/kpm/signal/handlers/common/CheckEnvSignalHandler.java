package org.kunlab.kpm.signal.handlers.common;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.plugin.PluginDescriptionFile;
import org.kunlab.kpm.TeamKunPluginManager;
import org.kunlab.kpm.installer.impls.install.signals.AlreadyInstalledPluginSignal;
import org.kunlab.kpm.interfaces.installer.signals.assertion.IgnoredPluginSignal;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.signal.SignalHandlingUtils;
import org.kunlab.kpm.task.tasks.install.signals.PluginIncompatibleWithKPMSignal;
import org.kunlab.kpm.utils.Utils;

/**
 * プラグインのインストール環境をチェックするハンドラです.
 */
public class CheckEnvSignalHandler
{
    private final Terminal terminal;

    public CheckEnvSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    @SignalHandler
    public void onIncompatibleWithKPM(PluginIncompatibleWithKPMSignal signal)
    {
        this.terminal.warn(LangProvider.get(
                "installer.common.checkenv.incompatible",
                MsgArgs.of("name", Utils.getPluginString(signal.getPluginDescription()))
        ));
        this.terminal.info(LangProvider.get("installer.operation.canForce"));

        signal.setForceInstall(SignalHandlingUtils.askContinue(this.terminal));
    }

    @SignalHandler
    public void onPluginIsIgnored(IgnoredPluginSignal signal)
    {
        if (!this.canForceInstall(signal.getPluginDescription()))
        {
            this.terminal.warn(LangProvider.get(
                    "installer.common.checkenv.excluded",
                    MsgArgs.of("name", Utils.getPluginString(signal.getPluginDescription()))
            ));

            signal.setContinueInstall(false);
            return;
        }

        this.terminal.warn(LangProvider.get(
                "installer.common.checkenv.excluded.force",
                MsgArgs.of("name", Utils.getPluginString(signal.getPluginDescription()))
        ));


        this.terminal.warn(LangProvider.get("installer.operation.forceWarn"));

        signal.setContinueInstall(SignalHandlingUtils.askContinue(this.terminal));
    }

    private void printKeyValue(String key, String value)
    {
        this.terminal.writeLine(LangProvider.get(
                "general.chat.writer.keyValue",
                MsgArgs.of("key", key)
                        .add("value", value)
        ));
    }

    private void printPluginInfo(PluginDescriptionFile descriptionFile)
    {
        this.printKeyValue("command.info.base.version", descriptionFile.getVersion());
        this.printKeyValue("command.info.base.authors", String.join(", ", descriptionFile.getAuthors()));
        this.printKeyValue("command.info.base.commands", String.join(", ", descriptionFile.getCommands().keySet()));
    }

    @SignalHandler
    public void onPluginIsDuplicated(AlreadyInstalledPluginSignal signal)
    {
        this.terminal.warn(LangProvider.get(
                "installer.common.checkenv.duplicate",
                MsgArgs.of("name", signal.getInstalledPlugin().getName())
        ));
        this.terminal.infoImplicit(LangProvider.get("installer.common.checkenv.duplicate.old"));
        this.printPluginInfo(signal.getInstalledPlugin());
        this.terminal.infoImplicit(LangProvider.get("installer.common.checkenv.duplicate.new"));
        this.printPluginInfo(signal.getInstallingPlugin());

        signal.setReplacePlugin(SignalHandlingUtils.askContinue(this.terminal));
    }

    private boolean canForceInstall(PluginDescriptionFile description)
    {
        return !description.getName().equals(TeamKunPluginManager.getPlugin().getName());
    }
}
