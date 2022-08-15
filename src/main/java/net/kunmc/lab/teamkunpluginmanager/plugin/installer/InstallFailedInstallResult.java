package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class InstallFailedInstallResult<P extends Enum<P>, T extends Enum<T>, S extends Enum<S>>
        extends InstallResult<P>
{
    @NotNull
    private final T reason;
    @Nullable
    private final S taskStatus;

    public InstallFailedInstallResult(@NotNull InstallProgress<P> progress, @NotNull T reason, @NotNull S taskStatus)
    {
        super(false, progress);
        this.reason = reason;
        this.taskStatus = taskStatus;
    }

    public InstallFailedInstallResult(InstallProgress<P> progress, @NotNull T reason)
    {
        super(false, progress);
        this.reason = reason;
        this.taskStatus = null;
    }
}
