package org.kunlab.kpm.installer.impls.autoremove;

import lombok.Getter;
import org.kunlab.kpm.installer.InstallResultImpl;
import org.kunlab.kpm.installer.interfaces.InstallProgress;
import org.kunlab.kpm.installer.interfaces.Installer;
import org.kunlab.kpm.task.tasks.uninstall.UnInstallResult;

/**
 * プラグインのアンインストールの結果を表すクラスです。
 */
@Getter
public class AutoRemoveSucceedResult extends InstallResultImpl<AutoRemoveTasks>
{
    private final UnInstallResult result;

    public AutoRemoveSucceedResult(InstallProgress<AutoRemoveTasks, Installer<AutoRemoveArgument, AutoRemoveErrorCause, AutoRemoveTasks>> progress, UnInstallResult result)
    {
        super(true, progress);
        this.result = result;
    }


}
