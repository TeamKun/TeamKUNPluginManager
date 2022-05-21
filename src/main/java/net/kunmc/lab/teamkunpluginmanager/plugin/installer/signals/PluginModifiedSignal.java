package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

@Value
public class PluginModifiedSignal implements InstallerSignal
{
    @NotNull
    PluginDescriptionFile pluginDescription;
    @NotNull
    ModifyType modifyType;

    public enum ModifyType
    {
        ADD,
        REMOVE,
        UPGRADE
    }
}
