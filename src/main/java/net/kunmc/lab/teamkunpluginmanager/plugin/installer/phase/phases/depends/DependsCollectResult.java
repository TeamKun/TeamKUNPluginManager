package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
public class DependsCollectResult extends PhaseResult<DependsCollectState, DependsCollectErrorCause>
{
    @NotNull
    String targetPlugin;

    @NotNull
    List<String> collectedPlugin;

    @NotNull
    List<String> collectingFailedPlugins;

    public DependsCollectResult(boolean success, @NotNull DependsCollectState phase,
                                @Nullable DependsCollectErrorCause errorCause, @NotNull String targetPlugin,
                                @NotNull List<String> collectedPlugin,
                                @NotNull List<String> collectingFailedPlugins)
    {
        super(success, phase, errorCause);
        this.targetPlugin = targetPlugin;
        this.collectedPlugin = collectedPlugin;
        this.collectingFailedPlugins = collectingFailedPlugins;
    }

    public boolean hasErrors()
    {
        return !collectingFailedPlugins.isEmpty();
    }
}
