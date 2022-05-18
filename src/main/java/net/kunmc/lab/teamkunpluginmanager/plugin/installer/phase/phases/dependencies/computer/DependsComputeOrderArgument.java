package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.computer;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.DependencyElement;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.collector.DependsCollectResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DependsComputeOrderArgument extends PhaseArgument
{
    @Getter
    private final List<DependencyElement> collectedDependencies;

    public DependsComputeOrderArgument(@NotNull DependsCollectResult previousPhaseResult)
    {
        super(previousPhaseResult);

        this.collectedDependencies = previousPhaseResult.getCollectedPlugins();
    }
}
