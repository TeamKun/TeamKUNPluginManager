package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.resolve;

import lombok.Data;
import lombok.NonNull;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;

@Data
public class PluginResolveErrorSignal implements InstallerSignal
{
    @NonNull
    private final ErrorResult error;
}
