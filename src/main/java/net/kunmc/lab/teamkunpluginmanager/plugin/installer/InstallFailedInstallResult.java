package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import lombok.Getter;

@Getter
public class InstallFailedInstallResult extends InstallResult
{
    private final FailedReason reason;

    InstallFailedInstallResult(InstallProgress progress, FailedReason reason)
    {
        super(false, progress);
        this.reason = reason;
    }

}
