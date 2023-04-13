package org.kunlab.kpm.signal.handlers.upgrade;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.Utils;
import org.kunlab.kpm.installer.InstallFailedInstallResult;
import org.kunlab.kpm.installer.impls.upgrade.UpgradeErrorCause;
import org.kunlab.kpm.installer.impls.upgrade.UpgradeTasks;
import org.kunlab.kpm.installer.interfaces.InstallResult;
import org.kunlab.kpm.signal.handlers.common.InstallFinishedSignalBase;

public class UpgradeFinishedSignalHandler extends InstallFinishedSignalBase
{
    private static final MsgArgs INSTALLER_NAME = MsgArgs.of("name", "%%installer.upgrade%%");

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
        String key;
        boolean named = false;
        switch (cause)
        {
            case UP_TO_DATE:  // THIS IS NOT AN ERROR
                key = "installer.upgrade.errors.up_to_date";
                break;
            case CANCELLED:
                key = "installer.upgrade.errors.cancel";
                named = true;
                break;
            case PLUGIN_NOT_FOUND:
                key = "installer.upgrade.errors.not_found";
                break;
            case INSTALL_FAILED:
                key = "installer.upgrade.errors.install.fail";
                break;
            case INSTALLER_INSTANTIATION_FAILED:
                key = "installer.upgrade.errors.install.constant";
                break;
            case PLUGIN_RESOLVE_FAILED:
                key = "installer.upgrade.errors.resolve";
                break;
            case PLUGIN_EXCLUDED:
                key = "installer.upgrade.errors.excluded";
                this.terminal.error(LangProvider.get(
                        key,
                        MsgArgs.of("name", "%%general.plugin.specified_plugin%%")
                ));
                return true;
            case UNINSTALL_FAILED:
                key = "installer.upgrade.errors.uninstall.fail";
                break;
            case UNINSTALLER_INSTANTIATION_FAILED:
                key = "installer.upgrade.errors.uninstall.constant";
                break;
            case SELF_UPGRADE_ATTEMPTED:
                key = "installer.upgrade.errors.self_upgrade";
                this.terminal.error(LangProvider.get(key));
                key = "installer.upgrade.errors.self_upgrade.suggest";
                this.terminal.hint(LangProvider.get(
                        key,
                        MsgArgs.of("command", "/kpm upgrade-kpm")
                ));
                return true;
            default:
                return false;
        }

        if (named)
            this.terminal.error(LangProvider.get(
                    key,
                    MsgArgs.of("name", "%%tasks.description%%")
            ));
        else
            this.terminal.error(LangProvider.get(key));

        return true;
    }

    @Override
    protected void onFail(InstallFailedInstallResult<?, ?, ?> result)
    {
        if (!(result.getProgress().getCurrentTask() instanceof UpgradeTasks))
            return;

        if (result.getReason() instanceof UpgradeErrorCause &&
                this.handleGeneralErrors((UpgradeErrorCause) result.getReason()))
            Utils.printInstallStatistics(this.terminal, result);
        else
            this.handleOtherError(result, INSTALLER_NAME);
    }
}
