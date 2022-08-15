package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Getter
public abstract class TaskResult<S extends Enum<?>, EC extends Enum<?>>
{
    private final boolean success;
    @NotNull
    private final S state;

    @Nullable
    private final EC errorCause;
}
