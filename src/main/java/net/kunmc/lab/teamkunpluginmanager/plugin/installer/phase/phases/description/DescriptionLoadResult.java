package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseResult;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class DescriptionLoadResult extends PhaseResult<DescriptionLoadState, DescriptionLoadErrorCause>
{
    @Nullable
    private final PluginDescriptionFile description;

    public DescriptionLoadResult(boolean success, @NotNull DescriptionLoadState phase,
                                 @Nullable DescriptionLoadErrorCause errorCause,
                                 @Nullable PluginDescriptionFile description)
    {
        super(success, phase, errorCause);
        this.description = description;
    }
}