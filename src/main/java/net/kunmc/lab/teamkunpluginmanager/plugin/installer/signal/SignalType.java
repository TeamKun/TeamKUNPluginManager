package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.ResolvingPluginSignal;

@AllArgsConstructor
@Getter
public enum SignalType
{
    RESOLVING_PLUGIN(ResolvingPluginSignal.class);

    private final Class<? extends InstallerSignal> signalClass;
}
