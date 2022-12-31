package net.kunmc.lab.kpm.installer.impls.autoremove;

import lombok.Getter;
import net.kunmc.lab.kpm.installer.InstallResultImpl;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.UnInstallResult;
import net.kunmc.lab.kpm.interfaces.installer.InstallProgress;

/**
 * プラグインのアンインストールの結果を表すクラスです。
 */
@Getter
public class AutoRemoveSucceedResult extends InstallResultImpl<AutoRemoveTasks>
{
    private final UnInstallResult result;

    public AutoRemoveSucceedResult(InstallProgress<AutoRemoveTasks, ?> progress, UnInstallResult result)
    {
        super(true, progress);
        this.result = result;
    }


}
