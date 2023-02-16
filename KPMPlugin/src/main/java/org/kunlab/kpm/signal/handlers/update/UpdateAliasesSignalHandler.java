package org.kunlab.kpm.signal.handlers.update;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.installer.InstallFailedInstallResult;
import org.kunlab.kpm.installer.impls.update.signals.UpdateFinishedSignal;
import org.kunlab.kpm.interfaces.installer.signals.InstallFinishedSignal;
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
        this.terminal.info("エイリアスが更新されました。");
        this.terminal.info("登録数：%d", signal.getAliases());
    }

    @SignalHandler
    public void onURLMalformed(MalformedURLSignal signal)
    {
        this.terminal.warn("不正なURLが指定されました(%s)： %s", signal.getRemoteName(), signal.getRemoteURL());
    }

    @SignalHandler
    public void onInvalidSources(InvalidSourceSignal signal)
    {
        switch (signal.getErrorCause())
        {
            case IO_ERROR:
                this.terminal.warn("不正なソースファイルが指定されました(%s)：IOError", signal.getSourceName());
                break;
            case SOURCE_FILE_MALFORMED:
                this.terminal.warn("不正なソースファイルが指定されました(%s)：Malformed", signal.getSourceName());
                break;
            default:
                this.terminal.warn("不正なソースファイルが指定されました(%s)：Unknown", signal.getSourceName());
        }
    }

    @SignalHandler
    public void onInstallFinished(InstallFinishedSignal signal)
    {
        if (signal.getResult().isSuccess())
            this.terminal.success("エイリアスの更新に成功しました。");
        else
        {
            InstallFailedInstallResult<?, ?, ?> result = (InstallFailedInstallResult<?, ?, ?>) signal.getResult();

            this.terminal.warn("エイリアスの更新は %s で失敗しました。", result.getReason());
        }
    }

}
