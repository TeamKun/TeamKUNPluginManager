package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.predicate;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.InstallPhase;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class PredicatePhase extends InstallPhase<PredicateArgument, PredicateResult>
{
    @NotNull
    private final Predicate<PredicateArgument> body;
    @Getter
    @NotNull
    private PredicateState phase;

    public PredicatePhase(@NotNull InstallProgress progress, @NotNull InstallerSignalHandler signalHandler,
                          @NotNull Predicate<PredicateArgument> predicate)
    {
        super(progress, signalHandler);
        this.phase = PredicateState.INITIALIZED;
        this.body = predicate;
    }

    @Override
    public @NotNull PredicateResult runPhase(@NotNull PredicateArgument argument)
    {
        this.phase = PredicateState.CHECK_RUN;

        try
        {
            if (this.body.test(argument))
                return new PredicateResult(true, this.phase);
            else
                return new PredicateResult(false, this.phase, PredicateErrorCause.CHECK_FAILED);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new PredicateResult(false, this.phase, PredicateErrorCause.EXCEPTION_OCCURRED);
        }
    }
}
