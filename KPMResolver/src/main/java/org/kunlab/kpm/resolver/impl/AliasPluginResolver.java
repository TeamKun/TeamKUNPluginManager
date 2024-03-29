package org.kunlab.kpm.resolver.impl;

import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.alias.interfaces.Alias;
import org.kunlab.kpm.alias.interfaces.AliasProvider;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.resolver.ErrorCause;
import org.kunlab.kpm.resolver.interfaces.BaseResolver;
import org.kunlab.kpm.resolver.interfaces.QueryContext;
import org.kunlab.kpm.resolver.interfaces.result.MultiResult;
import org.kunlab.kpm.resolver.interfaces.result.ResolveResult;
import org.kunlab.kpm.resolver.result.ErrorResultImpl;
import org.kunlab.kpm.resolver.result.PipeResult;

/**
 * エイリアスを使用してプラグインを解決するクラスです。
 */
public class AliasPluginResolver implements BaseResolver
{
    private final AliasProvider aliasProvider;

    public AliasPluginResolver(@NotNull KPMRegistry registry)
    {
        this.aliasProvider = registry.getAliasProvider();
    }

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        Alias alias = this.aliasProvider.getQueryByAlias(query.getQuery());

        if (alias == null)
            return new ErrorResultImpl(this, ErrorCause.RESOLVER_MISMATCH, ResolveResult.Source.LOCAL_KNOWN);

        query.setQuery(alias.getQuery());
        return new PipeResult(this, query);
    }

    @Override
    public ResolveResult autoPickOnePlugin(MultiResult multiResult)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidResolver(QueryContext query)
    {
        return true;
    }
}
