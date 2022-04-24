package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.resolve;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Data
public class PluginResolvedSuccessfulSignal implements InstallerSignal
{
    @NotNull
    private SuccessResult resolvedPlugin;
}
