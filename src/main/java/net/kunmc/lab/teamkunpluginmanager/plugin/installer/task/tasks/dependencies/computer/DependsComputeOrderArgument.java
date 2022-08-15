package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.computer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.DependencyElement;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.DependsCollectResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AllArgsConstructor
public class DependsComputeOrderArgument extends TaskArgument
{
    @Getter
    private final List<DependencyElement> collectedDependencies;

    public DependsComputeOrderArgument(@NotNull DependsCollectResult previousTaskResult)
    {
        super(previousTaskResult);

        this.collectedDependencies = previousTaskResult.getCollectedPlugins();
    }
}
