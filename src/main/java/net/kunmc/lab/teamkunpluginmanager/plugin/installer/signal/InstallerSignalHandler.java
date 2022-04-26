package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface InstallerSignalHandler
{
    <T extends InstallerSignal> void handleSignal(@NotNull InstallProgress installProgress, @NotNull T signal);
}
