package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.InstallPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.signals.MultiplePluginResolvedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.signals.PluginResolveErrorSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.signals.PluginResolvingSignal;
import net.kunmc.lab.teamkunpluginmanager.resolver.PluginResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PluginResolvePhase extends InstallPhase<PluginResolveArgument, PluginResolveResult>
{
    private static final PluginResolver PLUGIN_RESOLVER;

    static
    {
        PLUGIN_RESOLVER = TeamKunPluginManager.getPlugin().getResolver();
    }

    private PluginResolveState phaseState;

    public PluginResolvePhase(@NotNull InstallProgress progress, @NotNull InstallerSignalHandler signalHandler)
    {
        super(progress, signalHandler);

        this.phaseState = PluginResolveState.INITIALIZED;
    }

    private @Nullable ResolveResult resolveMultipleResults(@NotNull String query, @NotNull MultiResult results)
    {
        if (results.getResults().length < 1)
            throw new IllegalStateException("MultiResult with no results.");

        MultiplePluginResolvedSignal signal = new MultiplePluginResolvedSignal(query, results);
        this.postSignal(signal);

        if (signal.getSpecifiedResult() != null)
            return signal.getSpecifiedResult(); // Plugin actually resolved by SignalHandler.

        ResolveResult result = results.getResults()[0];

        if (result instanceof MultiResult)
        {
            MultiResult multiResult = (MultiResult) result;
            return this.resolveMultipleResults(query, multiResult); // Recursive call.
        }

        return result;
    }

    @Override
    public @NotNull PluginResolveResult runPhase(@NotNull PluginResolveArgument arguments)
    {
        String query = arguments.getQuery();

        this.phaseState = PluginResolveState.PRE_RESOLVING;
        this.postSignal(new PluginResolvingSignal(query, PLUGIN_RESOLVER));

        ResolveResult queryResolveResult = PLUGIN_RESOLVER.resolve(query);

        this.phaseState = PluginResolveState.PRE_RESOLVE_FINISHED;

        if (queryResolveResult instanceof ErrorResult)
        {
            this.postSignal(new PluginResolveErrorSignal((ErrorResult) queryResolveResult));
            return new PluginResolveResult(false, this.phaseState,
                    PluginResolveErrorCause.GOT_ERROR_RESULT, null
            );
        }
        else if (queryResolveResult instanceof MultiResult)
        {
            this.phaseState = PluginResolveState.MULTI_RESOLVING;

            MultiResult multiResult = (MultiResult) queryResolveResult;
            ResolveResult actualResolveResult = resolveMultipleResults(arguments.getQuery(), multiResult);

            if (actualResolveResult instanceof ErrorResult)
            {
                // MultiResult has been resolved, but the actual result is an error
                this.postSignal(new PluginResolveErrorSignal((ErrorResult) actualResolveResult));
                return new PluginResolveResult(false, this.phaseState, PluginResolveErrorCause.GOT_ERROR_RESULT, null);
            }

            // MultiResult has been resolved, and the actual result is a SuccessResult
            // (resolveMultipleResults() should not return a MultiResult)

            queryResolveResult = actualResolveResult;
        }

        this.phaseState = PluginResolveState.RESOLVE_FINISHED;

        if (!(queryResolveResult instanceof SuccessResult))
            return new PluginResolveResult(false, this.phaseState, PluginResolveErrorCause.ILLEGAL_INTERNAL_STATE, null);

        return new PluginResolveResult(true, this.phaseState, (SuccessResult) queryResolveResult);
    }
}
