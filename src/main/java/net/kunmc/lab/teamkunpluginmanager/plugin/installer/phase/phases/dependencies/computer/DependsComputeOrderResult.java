package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.computer;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.DependencyElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DependsComputeOrderResult extends PhaseResult<DependsComputeOrderState, DependsComputeOrderErrorCause>
{
    @Getter
    @NotNull
    private final List<DependencyElement> order;

    public DependsComputeOrderResult(boolean success, @NotNull DependsComputeOrderState phase,
                                     @Nullable DependsComputeOrderErrorCause errorCause,
                                     @NotNull List<DependencyElement> order)
    {
        super(success, phase, errorCause);
        this.order = order;
    }
}
