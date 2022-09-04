package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task;

import lombok.AllArgsConstructor;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public abstract class InstallTask<A extends TaskArgument, R extends TaskResult<? extends Enum<?>, ? extends Enum<?>>>
{
    @NotNull
    protected final InstallProgress<?> progress;
    @NotNull
    private final InstallerSignalHandler signalHandler;

    public abstract @NotNull R runTask(@NotNull A arguments);

    protected void postSignal(@NotNull InstallerSignal signal)
    {
        this.signalHandler.handleSignal(this.progress, signal);
    }
}
