package net.kunmc.lab.teamkunpluginmanager.plugin;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallFailedInstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.GeneralTaskErrorCause;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.InstallTask;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskSubmitter;
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
        this.progress = InstallProgress.of(signalHandler, null);
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
            @NotNull S taskStatus)
    {  // TODO: Implement debug mode
        return new InstallFailedInstallResult<>(this.progress, reason, taskStatus);
    }

    @NotNull
    @SuppressWarnings("rawtypes")
    protected InstallResult<P> handleTaskError(@NotNull TaskResult result)
    {
        if (result.getErrorCause() != null)
            return this.error(result.getErrorCause(), result.getState());
        else
            return this.error(GeneralTaskErrorCause.ILLEGAL_INTERNAL_STATE, result.getState());
    }

    @NotNull
    protected <A extends TaskArgument, R extends TaskResult<?, ?>, TT extends InstallTask<A, R>>
    TaskSubmitter<A, P, ? extends AbstractInstaller<E, P>, A, R, TT>
    submitter(@NotNull P taskState, @NotNull TT task)
    {
        return new TaskSubmitter<>(taskState, this, task);
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
