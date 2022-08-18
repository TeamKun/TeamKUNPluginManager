package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskResult;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class UnInstallResult extends TaskResult<UnInstallState, UnInstallErrorCause>
{
    @Getter
    @NotNull
    private final List<PluginDescriptionFile> uninstalledPlugins;
    @Getter
    @NotNull
    private final Map<UnInstallErrorCause, PluginDescriptionFile> errors;

    public UnInstallResult(boolean success, @NotNull UnInstallState state, @Nullable UnInstallErrorCause errorCause, @NotNull List<PluginDescriptionFile> uninstalledPlugins, @NotNull Map<UnInstallErrorCause, PluginDescriptionFile> errors)
    {
        super(success, state, errorCause);
        this.uninstalledPlugins = uninstalledPlugins;
        this.errors = errors;
    }
}
