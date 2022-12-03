package net.kunmc.lab.kpm.installer.impls.autoremove;

import lombok.Getter;
import net.kunmc.lab.kpm.installer.InstallProgress;
import net.kunmc.lab.kpm.installer.InstallResult;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.hard.UnInstallResult;

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
