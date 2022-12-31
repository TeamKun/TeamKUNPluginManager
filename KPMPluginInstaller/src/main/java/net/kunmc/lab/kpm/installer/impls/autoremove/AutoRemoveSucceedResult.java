package net.kunmc.lab.kpm.installer.impls.autoremove;

import lombok.Getter;
import net.kunmc.lab.kpm.installer.InstallResultImpl;
import net.kunmc.lab.kpm.interfaces.installer.InstallProgress;
import net.kunmc.lab.kpm.interfaces.installer.PluginInstaller;
import net.kunmc.lab.kpm.task.tasks.uninstall.UnInstallResult;

/**
 * プラグインのアンインストールの結果を表すクラスです。
 */
@Getter
public class AutoRemoveSucceedResult extends InstallResultImpl<AutoRemoveTasks>
{
    private final UnInstallResult result;

    public AutoRemoveSucceedResult(InstallProgress<AutoRemoveTasks, PluginInstaller<AutoRemoveArgument, AutoRemoveErrorCause, AutoRemoveTasks>> progress, UnInstallResult result)
    {
        super(true, progress);
        this.result = result;
    }


}
