package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PluginResolveResult extends PhaseResult<PluginResolveState, PluginResolveErrorCause>
{
    @Getter
    @Nullable
    private final SuccessResult resolveResult;

    public PluginResolveResult(boolean success, @NotNull PluginResolveState phase,
                               @NotNull PluginResolveErrorCause errorCause, @Nullable SuccessResult resolveResult)
    {
        super(success, phase, errorCause);
        this.resolveResult = resolveResult;
    }

    public PluginResolveResult(boolean success, @NotNull PluginResolveState phase, @NotNull SuccessResult resolveResult)
    {
        super(success, phase, null);
        this.resolveResult = resolveResult;
    }

}
