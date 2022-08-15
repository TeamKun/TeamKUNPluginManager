package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.computer;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.DependencyElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DependsComputeOrderResult extends TaskResult<DependsComputeOrderState, DependsComputeOrderErrorCause>
{
    @Getter
    @NotNull
    private final List<DependencyElement> order;

    public DependsComputeOrderResult(boolean success, @NotNull DependsComputeOrderState taskState,
                                     @Nullable DependsComputeOrderErrorCause errorCause,
                                     @NotNull List<DependencyElement> order)
    {
        super(success, taskState, errorCause);
        this.order = order;
    }
}
