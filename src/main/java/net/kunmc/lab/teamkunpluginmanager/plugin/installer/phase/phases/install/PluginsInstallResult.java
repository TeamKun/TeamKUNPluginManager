package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.install;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PluginsInstallResult extends PhaseResult<PluginsInstallState, PluginsInstallErrorCause>
{
    @Getter
    @Nullable
    private final String failedPluginName;

    public PluginsInstallResult(boolean success, @NotNull PluginsInstallState phase, @Nullable PluginsInstallErrorCause errorCause)
    {
        this(success, phase, errorCause, null);
    }

    public PluginsInstallResult(boolean success, @NotNull PluginsInstallState phase,
                                @Nullable PluginsInstallErrorCause errorCause, @Nullable String failedPluginName)
    {
        super(success, phase, errorCause);
        this.failedPluginName = failedPluginName;
    }
}
