package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.PluginResolvingSignal;

@AllArgsConstructor
@Getter
public enum SignalType
{
    RESOLVING_PLUGIN(PluginResolvingSignal.class);

    private final Class<? extends InstallerSignal> signalClass;
}
