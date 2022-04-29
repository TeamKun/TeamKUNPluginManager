package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.FailedReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Getter
public abstract class PhaseResult<R extends Enum<?> & PhaseEnum>
{
    private final boolean success;
    @NotNull
    private final R phase;

    @Nullable
    private final FailedReason errorCause;

}
