package net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignal;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Data
@AllArgsConstructor
public class LoadPluginDescriptionSignal implements InstallerSignal
{
    @NotNull
    private Path pluginFile;
}
