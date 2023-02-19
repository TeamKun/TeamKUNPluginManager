package org.kunlab.kpm.signal.handlers.uninstall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginDisablingSignal;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginRegisteredRecipeSignal;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginUninstallErrorSignal;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginUninstallingSignal;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginUnloadingSignal;
import org.kunlab.kpm.utils.Utils;

public class UninstallerSignalHandler
{
    private final Terminal terminal;
    private boolean oneRecipeRemoved;

    public UninstallerSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
        this.oneRecipeRemoved = false;
    }

    private static String getErrorCauseMessage(PluginUninstallErrorSignal signal)
    {
        String key;
        switch (signal.getCause())
        {
            case INTERNAL_CLASS_UNLOAD_FAILED:
                key = "installer.tasks.uninstall.errors.classUnloadFail";
                break;
            case INTERNAL_PLUGIN_DISABLE_FAILED:
                key = "installer.tasks.uninstall.errors.disableFail";
                break;
            default:
                key = "general.errors.unknown";
        }

        return LangProvider.get(key);
    }

    @SignalHandler
    public void onPluginUninstall(PluginUninstallingSignal signal)
    {
        this.terminal.infoImplicit(LangProvider.get(
                "installer.tasks.uninstall.uninstalling",
                MsgArgs.of("name", Utils.getPluginString(signal.getPlugin()))
        ));
    }

    @SignalHandler
    public void onRecipeRemove(PluginRegisteredRecipeSignal.Removing signal)
    {
        if (!this.oneRecipeRemoved)
        {
            this.terminal.infoImplicit(LangProvider.get(
                    "installer.tasks.uninstall.recipesRemoving",
                    MsgArgs.of("name", Utils.getPluginString(signal.getPlugin()))
            ));
            this.oneRecipeRemoved = true;
        }
    }

    @SignalHandler
    public void onDisabling(PluginDisablingSignal.Pre signal)
    {
        this.terminal.infoImplicit(LangProvider.get(
                "installer.tasks.uninstall.disabling",
                MsgArgs.of("name", Utils.getPluginString(signal.getPlugin()))
        ));
    }

    @SignalHandler
    public void onUnloading(PluginUnloadingSignal.Pre signal)
    {
        this.terminal.infoImplicit(LangProvider.get(
                        "installer.tasks.uninstall.unloading",
                        MsgArgs.of("name", Utils.getPluginString(signal.getPlugin()))
                )
        );
    }

    @SignalHandler
    public void onError(PluginUninstallErrorSignal signal)
    {
        this.terminal.error(LangProvider.get(
                "installer.tasks.uninstall.error",
                MsgArgs.of("name", Utils.getPluginString(signal.getDescription()))
                        .add("error", getErrorCauseMessage(signal))
        ));
    }
}
