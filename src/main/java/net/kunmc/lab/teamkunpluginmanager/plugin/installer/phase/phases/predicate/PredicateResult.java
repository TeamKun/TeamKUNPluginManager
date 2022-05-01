package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.predicate;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseResult;
import org.jetbrains.annotations.NotNull;

public class PredicateResult extends PhaseResult<PredicateState, PredicateErrorCause>
{
    public PredicateResult(boolean success, @NotNull PredicateState state, @NotNull PredicateErrorCause errorCause)
    {
        super(success, state, errorCause);
    }

    public PredicateResult(boolean success, PredicateState state)
    {
        super(success, state, null);
    }
}
