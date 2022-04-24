package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.resolve;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class MultiplePluginResolvedSignal implements InstallerSignal
{
    @NotNull
    private final String query;

    @NotNull
    private final MultiResult results;

    @Nullable
    private ResolveResult specifiedResult;
}
