package net.kunmc.lab.teamkunpluginmanager.plugin.meta;

import lombok.Value;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
public class PluginMeta
{
    @NotNull
    String name;
    @NotNull
    PluginDescriptionFile pluginYML;

    @NotNull
    PluginInstaller pluginInstaller;
    @NotNull
    PluginType pluginType;

    long installedIn;
    boolean enable;

    @Nullable
    String resolver;
}
