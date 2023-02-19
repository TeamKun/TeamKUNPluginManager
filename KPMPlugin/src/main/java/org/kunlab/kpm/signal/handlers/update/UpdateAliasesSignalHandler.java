package org.kunlab.kpm.signal.handlers.update;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.installer.InstallFailedInstallResult;
import org.kunlab.kpm.installer.impls.update.signals.UpdateFinishedSignal;
import org.kunlab.kpm.interfaces.installer.signals.InstallFinishedSignal;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.task.tasks.alias.source.download.signals.MalformedURLSignal;
import org.kunlab.kpm.task.tasks.alias.update.signals.InvalidSourceSignal;

public class UpdateAliasesSignalHandler
{
    private final Terminal terminal;

    public UpdateAliasesSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    @SignalHandler
    public void onUpdateAliases(UpdateFinishedSignal signal)
    {
        this.terminal.info(LangProvider.get("installer.update.done"));
        this.terminal.info(LangProvider.get(
                "installer.update.aliases",
                MsgArgs.of("aliases", signal.getAliases())
        ));
    }

    @SignalHandler
    public void onURLMalformed(MalformedURLSignal signal)
    {
        this.terminal.warn(LangProvider.get(
                "tasks.update.malformed_url",
                MsgArgs.of("remote", signal.getRemoteName())
                        .add("url", signal.getRemoteURL())
        ));
    }

    @SignalHandler
    public void onInvalidSources(InvalidSourceSignal signal)
    {
        String key;
        switch (signal.getErrorCause())
        {
            case IO_ERROR:
                key = "tasks.update.invalid_source.io_exception";
                break;
            case SOURCE_FILE_MALFORMED:
                key = "tasks.update.invalid_source.malformed_url";
                break;
            default:
                key = "tasks.update.invalid_source.unknown";
                break;
        }

        this.terminal.warn(LangProvider.get(
                key,
                MsgArgs.of("source", signal.getSourceName())
        ));
    }

    @SignalHandler
    public void onInstallFinished(InstallFinishedSignal signal)
    {
        if (signal.getResult().isSuccess())
            this.terminal.success(LangProvider.get(
                    "installer.update.success",
                    MsgArgs.of("name", "%%installer.update%%")
            ));
        else
        {
            InstallFailedInstallResult<?, ?, ?> result = (InstallFailedInstallResult<?, ?, ?>) signal.getResult();

            this.terminal.warn(LangProvider.get(
                    "installer.update.fail",
                    MsgArgs.of("error", result.getReason())
            ));
        }
    }

}
