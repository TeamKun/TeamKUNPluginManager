package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface InstallerSignalHandler
{
    <T extends InstallerSignal> void handleSignal(@NotNull T signal);
}
