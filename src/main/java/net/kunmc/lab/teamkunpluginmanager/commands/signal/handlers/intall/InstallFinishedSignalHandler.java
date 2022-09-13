package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.intall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.common.InstallFinishedSignalBase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallFailedInstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;

/**
 * インストールが完了したときのシグナルを処理するハンドラーです。
 */
public class InstallFinishedSignalHandler extends InstallFinishedSignalBase
{
    public InstallFinishedSignalHandler(Terminal terminal)
    {
        super(terminal);
    }

    @Override
    protected void onSuccess(InstallResult<?> result)
    {
        this.terminal.success("インストールが正常に完了しました。");
    }

    @Override
    protected void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        this.terminal.error("インストールに失敗しました。");
    }
}
