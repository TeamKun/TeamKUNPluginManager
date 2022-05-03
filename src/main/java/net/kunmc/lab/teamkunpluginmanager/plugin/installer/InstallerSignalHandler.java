package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface InstallerSignalHandler
{
    <T extends InstallerSignal> void handleSignal(@NotNull InstallProgress<?> installProgress, @NotNull T signal);
}
