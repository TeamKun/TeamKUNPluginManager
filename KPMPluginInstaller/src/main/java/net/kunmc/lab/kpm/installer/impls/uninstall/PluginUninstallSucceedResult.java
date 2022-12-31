package net.kunmc.lab.kpm.installer.impls.uninstall;

import lombok.Getter;
import net.kunmc.lab.kpm.installer.InstallResultImpl;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.UnInstallResult;
import net.kunmc.lab.kpm.interfaces.installer.InstallProgress;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;
import net.kunmc.lab.kpm.interfaces.installer.PluginInstaller;

/**
 * プラグインのアンインストールの結果を表すクラスです。
 */
@Getter
public class PluginUninstallSucceedResult extends InstallResultImpl<UnInstallTasks>
{
    private final UnInstallResult result;

    public PluginUninstallSucceedResult(InstallProgress<UnInstallTasks, ? extends PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, UnInstallTasks>> progress, UnInstallResult result)
    {
        super(true, progress);
        this.result = result;
    }
}
