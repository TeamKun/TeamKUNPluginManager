package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

@Value
@EqualsAndHashCode(callSuper = false)
public class DownloadArgument extends PhaseArgument
{
    @NotNull
    String url;

    @Nullable
    Path path;

    public DownloadArgument(@NotNull String url, @Nullable Path path)
    {
        this.url = url;
        this.path = path;
    }

    public DownloadArgument(@NotNull String url)
    {
        this.url = url;
        this.path = null;
    }

    public DownloadArgument(@NotNull PluginResolveResult pluginResolveResult)
    {
        super(pluginResolveResult);

        if (pluginResolveResult.getResolveResult() == null)
            throw new IllegalArgumentException("Plugin Resolving must be successful");

        this.url = pluginResolveResult.getResolveResult().getDownloadUrl();
        this.path = null;
    }
}
