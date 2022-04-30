package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase;

import lombok.AllArgsConstructor;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public abstract class InstallPhase<A extends PhaseArgument, R extends PhaseResult<? extends Enum<?>, ? extends Enum<?>>>
{
    @NotNull
    protected final InstallProgress progress;
    @NotNull
    private final InstallerSignalHandler signalHandler;

    public abstract @NotNull R runPhase(@NotNull A arguments);

    public void postSignal(@NotNull InstallerSignal signal)
    {
        this.signalHandler.handleSignal(this.progress, signal);
    }
}
