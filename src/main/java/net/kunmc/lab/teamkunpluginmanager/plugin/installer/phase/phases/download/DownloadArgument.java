package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolveResult;
import org.jetbrains.annotations.NotNull;

@Value
public class DownloadArgument implements PhaseArgument
{
    @NotNull
    String url;

    public static DownloadArgument of(@NotNull PluginResolveResult pluginResolveResult)
    {
        if (!pluginResolveResult.isSuccess() || pluginResolveResult.getResolveResult() == null)
            throw new IllegalArgumentException("Plugin Resolving must be successful");

        return new DownloadArgument(pluginResolveResult.getResolveResult().getDownloadUrl());
    }
}
