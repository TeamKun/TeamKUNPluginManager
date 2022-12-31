package net.kunmc.lab.kpm.signal.handlers.upgrade;

import net.kunmc.lab.kpm.Utils;
import net.kunmc.lab.kpm.installer.InstallFailedInstallResult;
import net.kunmc.lab.kpm.installer.impls.upgrade.UpgradeErrorCause;
import net.kunmc.lab.kpm.installer.impls.upgrade.UpgradeTasks;
import net.kunmc.lab.kpm.interfaces.installer.InstallResult;
import net.kunmc.lab.kpm.signal.handlers.common.InstallFinishedSignalBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;

public class UpgradeFinishedSignalHandler extends InstallFinishedSignalBase
{
    public UpgradeFinishedSignalHandler(Terminal terminal)
    {
        super(terminal);
        this.setPrintResult(false);
    }

    @Override
    protected void onSuccess(InstallResult<? extends Enum<?>> result)
    {
        if (!(result.getProgress().getCurrentTask() instanceof UpgradeTasks))
            return;

        Utils.printInstallStatistics(this.terminal, result);
    }

    private boolean handleGeneralErrors(UpgradeErrorCause cause)
    {
        switch (cause)
        {
            case UP_TO_DATE:  // THIS IS NOT AN ERROR
                this.terminal.success("プラグインが最新です。");
                return true;
            case CANCELLED:
                this.terminal.error("アップグレードがキャンセルされました。");
                return true;
            case PLUGIN_NOT_FOUND:
                this.terminal.error("指定されたプラグインが見つかりませんでした。");
                return true;
            case INSTALL_FAILED:
                this.terminal.error("プラグインのインストールに失敗しました。");
                return true;
            case INSTALLER_INSTANTIATION_FAILED:
                this.terminal.error("プラグインインストーラの生成に失敗しました。");
                return true;
            case PLUGIN_RESOLVE_FAILED:
                this.terminal.error("プラグインの解決に失敗しました。");
                return true;
            case PLUGIN_EXCLUDED:
                this.terminal.error("指定されたプラグインが除外リストに登録されています。");
                return true;
            case UNINSTALL_FAILED:
                this.terminal.error("プラグインのアンインストールに失敗しました。");
                return true;
            case UNINSTALLER_INSTANTIATION_FAILED:
                this.terminal.error("プラグインアンインストーラの生成に失敗しました。");
                return true;
            case SELF_UPGRADE_ATTEMPTED:
                this.terminal.error("KPM 自体のアップグレードを試みました。");
                this.terminal.hint("KPM のアップグレードは、 /kpm upgrade-kpm コマンドを使用してください。");
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        if (!(result.getProgress().getCurrentTask() instanceof UpgradeTasks))
            return;

        if (result.getException() != null)
        {
            this.terminal.error("アップグレード中に予期しないエラーが発生しました：%s", result.getException());
            Utils.printInstallStatistics(this.terminal, result);
            return;
        }

        if (result.getReason() instanceof UpgradeErrorCause &&
                this.handleGeneralErrors((UpgradeErrorCause) result.getReason()))
        {
            Utils.printInstallStatistics(this.terminal, result);
            return;
        }

        this.terminal.error("アップグレード中に予期しないエラーが発生しました。");
    }
}
