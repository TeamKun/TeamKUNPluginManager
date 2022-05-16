package net.kunmc.lab.teamkunpluginmanager.plugin;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallFailedInstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.GeneralPhaseErrorCause;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.InstallPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseSubmitter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public abstract class AbstractInstaller<E extends Enum<E>, P extends Enum<P>>
{
    @Getter
    protected final InstallProgress<P> progress;
    protected final InstallerSignalHandler signalHandler;

    public AbstractInstaller(InstallerSignalHandler signalHandler) throws IOException
    {
        this.progress = InstallProgress.of(null);
        this.signalHandler = signalHandler;
    }

    protected void postSignal(@NotNull InstallerSignal signal)
    {
        this.signalHandler.handleSignal(this.progress, signal);
    }

    public abstract InstallResult<P> execute(@NotNull String query) throws IOException;

    @NotNull
    protected InstallResult<P> success()
    {
        return new InstallResult<>(true, this.progress);
    }

    public <T extends Enum<T>> InstallFailedInstallResult<P, T, ?> error(@NotNull T reason)
    {  // TODO: Implement debug mode
        return new InstallFailedInstallResult<>(this.progress, reason);
    }

    public <T extends Enum<T>, S extends Enum<S>> InstallFailedInstallResult<P, T, S> error(
            @NotNull T reason,
            @NotNull S phaseStatus)
    {  // TODO: Implement debug mode
        return new InstallFailedInstallResult<>(this.progress, reason, phaseStatus);
    }

    @NotNull
    protected <S extends Enum<S>, T extends Enum<T>> InstallResult<P> handlePhaseError(@NotNull PhaseResult<S, T> result)
    {
        if (result.getErrorCause() != null)
            return this.error(result.getErrorCause(), result.getPhase());
        else
            return this.error(GeneralPhaseErrorCause.ILLEGAL_INTERNAL_STATE, result.getPhase());
    }

    @NotNull
    protected <A extends PhaseArgument, R extends PhaseResult<?, ?>, PP extends InstallPhase<A, R>>
    PhaseSubmitter<A, P, ? extends AbstractInstaller<E, P>, A, R, PP>
    submitter(@NotNull P phaseState, @NotNull PP phase)
    {
        return new PhaseSubmitter<>(phaseState, this, phase);
    }

    protected boolean isPluginIgnored(@NotNull String pluginName)
    {
        return TeamKunPluginManager.getPlugin().getPluginConfig().getStringList("ignore").stream()
                .parallel()
                .anyMatch(s -> s.equalsIgnoreCase(pluginName));
    }

    protected boolean safeDelete(@NotNull File f)
    {
        try
        {
            return f.delete();
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
