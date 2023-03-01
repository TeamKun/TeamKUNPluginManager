package org.kunlab.kpm.signal.handlers.intall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.installer.signals.InvalidKPMInfoFileSignal;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.signal.SignalHandlingUtils;
import org.kunlab.kpm.task.tasks.install.signals.PluginEnablingSignal;
import org.kunlab.kpm.task.tasks.install.signals.PluginInstallingSignal;
import org.kunlab.kpm.task.tasks.install.signals.PluginLoadSignal;
import org.kunlab.kpm.task.tasks.install.signals.PluginRelocatingSignal;
import org.kunlab.kpm.utils.Utils;

/**
 * インストーラのシグナルをハンドルするハンドラです.
 */
public class InstallerSignalHandler
{
    private final Terminal terminal;

    public InstallerSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    // Ignore PluginOnLoadRunningSignal

    @SignalHandler
    public void onPluginInstallStart(PluginInstallingSignal signal)
    {
        String name = signal.getPluginDescription().getName();
        this.terminal.infoImplicit(LangProvider.get(
                "tasks.install.preparing",
                MsgArgs.of("name", name)
        ));
    }

    @SignalHandler
    public void onPluginRelocating(PluginRelocatingSignal signal)
    {
        String src = "…/" + signal.getSource().getFileName();
        String dest = "…/" + signal.getTarget().getFileName();
        this.terminal.infoImplicit(LangProvider.get(
                "tasks.install.relocating",
                MsgArgs.of("src", src).add("dest", dest)
        ));
    }

    @SignalHandler
    public void onPluginLoadPre(PluginLoadSignal.Pre signal)
    {
        this.terminal.infoImplicit(LangProvider.get("tasks.install.loading", MsgArgs.of("name", signal.getPluginDescription().getName())));
    }

    @SignalHandler
    public void onPluginLoading(PluginEnablingSignal.Pre signal)
    {
        this.terminal.infoImplicit(LangProvider.get(
                "tasks.install.enabling",
                MsgArgs.of("name", Utils.getPluginString(signal.getPlugin()))
        ));
    }

    @SignalHandler
    public void onInvalidKPMInfoFile(InvalidKPMInfoFileSignal signal)
    {
        this.terminal.warn(LangProvider.get(
                "tasks.install.invalid_kpm_info",
                MsgArgs.of("name", signal.getDescriptionFile().getName())
        ));
        this.terminal.hint(
                LangProvider.get("tasks.install.invalid_kpm_info.force")
        );

        signal.setIgnore(SignalHandlingUtils.askContinue(this.terminal));
    }
}
