package net.kunmc.lab.kpm.resolver.impl;

import net.kunmc.lab.kpm.enums.resolver.ErrorCause;
import net.kunmc.lab.kpm.interfaces.KPMRegistry;
import net.kunmc.lab.kpm.interfaces.alias.Alias;
import net.kunmc.lab.kpm.interfaces.alias.AliasProvider;
import net.kunmc.lab.kpm.interfaces.resolver.BaseResolver;
import net.kunmc.lab.kpm.interfaces.resolver.result.MultiResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.resolver.result.ErrorResultImpl;
import net.kunmc.lab.kpm.resolver.result.PipeResult;
import org.jetbrains.annotations.NotNull;

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
