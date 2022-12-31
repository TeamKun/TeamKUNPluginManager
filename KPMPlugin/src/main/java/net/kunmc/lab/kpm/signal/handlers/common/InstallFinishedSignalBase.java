package net.kunmc.lab.kpm.signal.handlers.common;

import lombok.AccessLevel;
import lombok.Setter;
import net.kunmc.lab.kpm.Utils;
import net.kunmc.lab.kpm.installer.InstallFailedInstallResult;
import net.kunmc.lab.kpm.interfaces.installer.InstallResult;
import net.kunmc.lab.kpm.interfaces.installer.signals.InstallFinishedSignal;
import net.kunmc.lab.kpm.signal.SignalHandler;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;

/**
 * インストールが完了したときのシグナルを処理するハンドラーの基底クラスです。
 * デフォルトでは, インストール結果が表示された後に, {@link #onSuccess(InstallResult)} または {@link #onFail(InstallFailedInstallResult)} が呼び出されます.
 */
public abstract class InstallFinishedSignalBase
{
    protected final Terminal terminal;
    /**
     * インストール結果を表示するかどうかです。
     */
    @Setter(AccessLevel.PROTECTED)
    private boolean printResult;

    public InstallFinishedSignalBase(Terminal terminal)
    {
        this.terminal = terminal;
        this.printResult = true;
    }

    @SignalHandler
    public void onFinished(InstallFinishedSignal finished)
    {
        InstallResult<? extends Enum<?>> installResult = finished.getResult();

        if (this.printResult)
            Utils.printInstallStatistics(this.terminal, installResult);

        if (finished.getResult() instanceof InstallFailedInstallResult)
        {
            this.onFail((InstallFailedInstallResult<?, ?, ?>) finished.getResult());
        }
        else
            this.onSuccess(installResult);
    }

    /**
     * インストールが成功したときに呼び出されます。
     *
     * @param result インストールの結果
     */
    protected abstract void onSuccess(InstallResult<? extends Enum<?>> result);

    /**
     * インストールが失敗したときに呼び出されます。
     *
     * @param result インストールの結果
     */
    protected abstract void onFail(InstallFailedInstallResult<?, ?, ?> result);

}
