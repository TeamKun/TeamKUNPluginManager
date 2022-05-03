package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase;

import lombok.Getter;

public abstract class PhaseArgument
{
    @Getter
    private final boolean chain;

    public PhaseArgument(PhaseResult<?, ?> previousPhaseResult)
    {
        this.chain = true;
        if (!previousPhaseResult.isSuccess())
            throw new IllegalStateException("Previous must be successful");

    }

    public PhaseArgument()
    {
        this.chain = false;
    }
}
