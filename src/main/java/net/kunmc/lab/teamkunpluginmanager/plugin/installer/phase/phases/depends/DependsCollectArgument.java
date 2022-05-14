package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadResult;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode(callSuper = false)
public class DependsCollectArgument extends PhaseArgument
{
    @NotNull
    PluginDescriptionFile pluginDescription;
    @NotNull
    List<String> alreadyInstalledPlugins;

    public DependsCollectArgument(@NotNull DescriptionLoadResult descriptionLoadResult)
    {
        super(descriptionLoadResult);

        if (descriptionLoadResult.getDescription() == null)
            throw new IllegalStateException("descriptionLoadResult.description is null");
        this.pluginDescription = descriptionLoadResult.getDescription();

        this.alreadyInstalledPlugins = Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                .map(plugin -> plugin.getDescription().getName())
                .collect(Collectors.toList());
    }

    public DependsCollectArgument(@NotNull PluginDescriptionFile pluginDescription,
                                  @NotNull List<String> alreadyInstalledPlugins)
    {
        this.pluginDescription = pluginDescription;
        this.alreadyInstalledPlugins = alreadyInstalledPlugins;
    }

    public DependsCollectArgument(@NotNull PluginDescriptionFile pluginDescription)
    {
        this(pluginDescription, Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                .map(plugin -> plugin.getDescription().getName())
                .collect(Collectors.toList()));
    }
}
