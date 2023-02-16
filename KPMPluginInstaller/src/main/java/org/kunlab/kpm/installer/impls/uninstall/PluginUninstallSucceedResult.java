package org.kunlab.kpm.installer.impls.uninstall;

import lombok.Getter;
import org.kunlab.kpm.installer.InstallResultImpl;
import org.kunlab.kpm.interfaces.installer.InstallProgress;
import org.kunlab.kpm.interfaces.installer.InstallerArgument;
import org.kunlab.kpm.interfaces.installer.PluginInstaller;
import org.kunlab.kpm.task.tasks.uninstall.UnInstallResult;

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
