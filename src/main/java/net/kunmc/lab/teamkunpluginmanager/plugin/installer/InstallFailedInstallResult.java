package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseEnum;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class InstallFailedInstallResult extends InstallResult
{
    @NotNull
    private final FailedReason reason;
    @Nullable
    private final PhaseEnum phaseStatus;

    InstallFailedInstallResult(@NotNull InstallProgress progress, @NotNull FailedReason reason, @NotNull PhaseEnum phaseStatus)
    {
        super(false, progress);
        this.reason = reason;
        this.phaseStatus = phaseStatus;
    }

    public InstallFailedInstallResult(InstallProgress progress, @NotNull FailedReason reason)
    {
        super(false, progress);
        this.reason = reason;
        this.phaseStatus = null;
    }
}
