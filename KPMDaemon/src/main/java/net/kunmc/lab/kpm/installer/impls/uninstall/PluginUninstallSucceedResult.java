package net.kunmc.lab.kpm.installer.impls.uninstall;

import lombok.Getter;
import net.kunmc.lab.kpm.installer.InstallProgress;
import net.kunmc.lab.kpm.installer.InstallResult;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.UnInstallResult;

/**
 * プラグインのアンインストールの結果を表すクラスです。
 */
@Getter
public class PluginUninstallSucceedResult extends InstallResult<UnInstallTasks>
{
    private final UnInstallResult result;

    public PluginUninstallSucceedResult(InstallProgress<UnInstallTasks, ?> progress, UnInstallResult result)
    {
        super(true, progress);
        this.result = result;
    }
}
