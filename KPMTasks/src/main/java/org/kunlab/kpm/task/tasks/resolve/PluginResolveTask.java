package org.kunlab.kpm.task.tasks.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.interfaces.installer.InstallerArgument;
import org.kunlab.kpm.interfaces.installer.PluginInstaller;
import org.kunlab.kpm.interfaces.resolver.PluginResolver;
import org.kunlab.kpm.interfaces.resolver.result.ErrorResult;
import org.kunlab.kpm.interfaces.resolver.result.MultiResult;
import org.kunlab.kpm.interfaces.resolver.result.ResolveResult;
import org.kunlab.kpm.interfaces.resolver.result.SuccessResult;
import org.kunlab.kpm.task.AbstractInstallTask;
import org.kunlab.kpm.task.tasks.resolve.signals.MultiplePluginResolvedSignal;
import org.kunlab.kpm.task.tasks.resolve.signals.PluginResolveErrorSignal;
import org.kunlab.kpm.task.tasks.resolve.signals.PluginResolvingSignal;

/**
 * プラグインの解決を行うタスクです。
 */
public class PluginResolveTask extends AbstractInstallTask<PluginResolveArgument, PluginResolveResult>
{
    private final PluginResolver resolver;

    private PluginResolveState taskState;

    public PluginResolveTask(@NotNull PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>> installer)
    {
        super(installer.getProgress(), installer.getProgress().getSignalHandler());
        this.resolver = installer.getRegistry().getPluginResolver();

        this.taskState = PluginResolveState.INITIALIZED;
    }

    private @Nullable ResolveResult resolveMultipleResults(@NotNull String query, @NotNull MultiResult results)
    {
        if (results.getResults().length < 1)
            throw new IllegalStateException("MultiResult with no results.");

        MultiplePluginResolvedSignal signal = new MultiplePluginResolvedSignal(query, results);
        this.postSignal(signal);

        if (signal.isCancel())
            return null;

        if (signal.getSpecifiedResult() != null)
            return signal.getSpecifiedResult(); // Plugin actually resolved by SignalHandler.

        return this.resolver.pickUpOne(results);
    }

    @Override
    public @NotNull PluginResolveResult runTask(@NotNull PluginResolveArgument arguments)
    {
        String query = arguments.getQuery();

        this.taskState = PluginResolveState.PRE_RESOLVING;
        this.postSignal(new PluginResolvingSignal(this.resolver, query));

        ResolveResult queryResolveResult = this.resolver.resolve(query);

        this.taskState = PluginResolveState.PRE_RESOLVE_FINISHED;

        if (queryResolveResult instanceof ErrorResult)
        {
            this.postSignal(new PluginResolveErrorSignal((ErrorResult) queryResolveResult, query));
            return new PluginResolveResult(false, this.taskState,
                    PluginResolveErrorCause.GOT_ERROR_RESULT, null
            );
        }
        else if (queryResolveResult instanceof MultiResult)
        {
            this.taskState = PluginResolveState.MULTI_RESOLVING;

            MultiResult multiResult = (MultiResult) queryResolveResult;
            ResolveResult actualResolveResult = this.resolveMultipleResults(arguments.getQuery(), multiResult);

            if (actualResolveResult == null)
                return new PluginResolveResult(false, this.taskState,
                        PluginResolveErrorCause.CANCELLED, null
                );

            if (actualResolveResult instanceof ErrorResult)
            {
                // MultiResult has been resolved, but the actual result is an error
                this.postSignal(new PluginResolveErrorSignal((ErrorResult) actualResolveResult, query));
                return new PluginResolveResult(false, this.taskState, PluginResolveErrorCause.GOT_ERROR_RESULT, null);
            }

            // MultiResult has been resolved, and the actual result is a SuccessResult
            // (resolveMultipleResults() should not return a MultiResult)

            queryResolveResult = actualResolveResult;
        }

        this.taskState = PluginResolveState.RESOLVE_FINISHED;

        if (!(queryResolveResult instanceof SuccessResult))
            return new PluginResolveResult(false, this.taskState, PluginResolveErrorCause.ILLEGAL_INTERNAL_STATE, null);

        return new PluginResolveResult(true, this.taskState, (SuccessResult) queryResolveResult);
    }
}
