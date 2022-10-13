package net.kunmc.lab.teamkunpluginmanager.installer.impls.autoremove;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.uninstall.UnInstallResult;

/**
 * プラグインのアンインストールの結果を表すクラスです。
 */
@Getter
public class AutoRemoveSucceedResult extends InstallResult<AutoRemoveTasks>
{
    private final UnInstallResult result;

    public AutoRemoveSucceedResult(InstallProgress<AutoRemoveTasks, ?> progress, UnInstallResult result)
    {
        super(true, progress);
        this.result = result;
    }
}
