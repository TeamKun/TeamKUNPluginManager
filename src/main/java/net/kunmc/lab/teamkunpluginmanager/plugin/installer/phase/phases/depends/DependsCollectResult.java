package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
public class DependsCollectResult extends PhaseResult<DependsCollectState, DependsCollectErrorCause>
{
    @NotNull
    String targetPlugin;

    @NotNull
    HashMap<String, Path> collectedPlugins;

    @NotNull
    List<String> collectFailedPlugins;

    public DependsCollectResult(boolean success, @NotNull DependsCollectState phase,
                                @Nullable DependsCollectErrorCause errorCause, @NotNull String targetPlugin,
                                @NotNull HashMap<String, Path> collectedPlugins,
                                @NotNull List<String> collectFailedPlugins)
    {
        super(success, phase, errorCause);
        this.targetPlugin = targetPlugin;
        this.collectedPlugins = collectedPlugins;
        this.collectFailedPlugins = collectFailedPlugins;
    }

    public boolean hasErrors()
    {
        return !collectFailedPlugins.isEmpty();
    }
}
