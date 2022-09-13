package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.uninstall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.common.InstallFinishedSignalBase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallFailedInstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;

/**
 * アンインストールが完了したときのシグナルを処理するハンドラーです。
 */
public class UninstallFinishedSignalHandler extends InstallFinishedSignalBase
{
    public UninstallFinishedSignalHandler(Terminal terminal)
    {
        super(terminal);
    }

    @Override
    protected void onSuccess(InstallResult<?> result)
    {
        this.terminal.success("アンインストールが正常に完了しました。");
    }

    @Override
    protected void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        this.terminal.error("アンインストールに失敗しました。");
    }
}
