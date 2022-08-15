package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Getter
public abstract class TaskResult<R extends Enum<?>, C extends Enum<?>>
{
    private final boolean success;
    @NotNull
    private final R task;

    @Nullable
    private final C errorCause;
}
