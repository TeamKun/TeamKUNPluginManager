package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve.signals;

import lombok.Data;
import lombok.NonNull;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;

@Data
public class PluginResolveErrorSignal implements InstallerSignal
{
    @NonNull
    private final ErrorResult error;
}
