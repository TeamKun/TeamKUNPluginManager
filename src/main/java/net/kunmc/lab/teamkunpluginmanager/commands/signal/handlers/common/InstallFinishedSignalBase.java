package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.common;

import lombok.AccessLevel;
import lombok.Setter;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallFailedInstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.InstallFinishedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.utils.Utils;

/**
 * インストールが完了したときのシグナルを処理するハンドラーの基底クラスです。
 * デフォルトでは, インストール結果が表示された後に, {@link #onSuccess(InstallResult)} または {@link #onFail(InstallFailedInstallResult)} が呼び出されます.
 */
public abstract class InstallFinishedSignalBase<P extends Enum<P>, T extends Enum<T>>
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
        InstallResult<?> installResult = finished.getResult();

        if (printResult)
            Utils.printInstallStatistics(terminal, installResult);

        if (finished.getResult() instanceof InstallFailedInstallResult)
        {
            try
            {
                //noinspection unchecked
                onFail((InstallFailedInstallResult<P, T, ?>) installResult);
            }
            catch (ClassCastException e)
            {
                onFailGeneral((InstallFailedInstallResult<?, ?, ?>) installResult);
            }
        }
        else
            onSuccess(installResult);
    }

    /**
     * インストールが成功したときに呼び出されます。
     *
     * @param result インストールの結果
     */
    protected abstract void onSuccess(InstallResult<?> result);

    /**
     * インストールが失敗したときに呼び出されます。
     *
     * @param result インストールの結果
     */
    protected abstract void onFail(InstallFailedInstallResult<P, T, ?> result);

    /**
     * インストールが失敗し, かつ型変換に失敗したときに呼び出されます。
     *
     * @param result インストールの結果
     */
    protected void onFailGeneral(InstallFailedInstallResult<?, ?, ?> result)
    {
        Enum<?> progress = result.getProgress().getCurrentTask();
        Enum<?> reason = result.getReason();
        Enum<?> taskStatus = result.getTaskStatus();


        String errorMessage = "操作は %s(%s) 時に %s により失敗しました。";

        terminal.error(String.format(
                errorMessage,
                progress,
                taskStatus,
                reason
        ));
    }
}
