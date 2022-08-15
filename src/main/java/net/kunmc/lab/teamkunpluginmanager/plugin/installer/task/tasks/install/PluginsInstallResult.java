package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PluginsInstallResult extends TaskResult<PluginsInstallState, PluginsInstallErrorCause>
{
    @Getter
    @Nullable
    private final String failedPluginName;

    public PluginsInstallResult(boolean success, @NotNull PluginsInstallState taskState, @Nullable PluginsInstallErrorCause errorCause)
    {
        this(success, taskState, errorCause, null);
    }

    public PluginsInstallResult(boolean success, @NotNull PluginsInstallState taskState,
                                @Nullable PluginsInstallErrorCause errorCause, @Nullable String failedPluginName)
    {
        super(success, taskState, errorCause);
        this.failedPluginName = failedPluginName;
    }
}
