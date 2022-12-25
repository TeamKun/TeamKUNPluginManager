package net.kunmc.lab.kpm.alias;

import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.resolver.interfaces.BaseResolver;
import net.kunmc.lab.kpm.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.resolver.result.MultiResult;
import net.kunmc.lab.kpm.resolver.result.PipeResult;
import net.kunmc.lab.kpm.resolver.result.ResolveResult;
import org.jetbrains.annotations.NotNull;

/**
 * エイリアスを使用してプラグインを解決するクラスです。
 */
public class AliasPluginResolver implements BaseResolver
{
    private final AliasProvider aliasProvider;

    public AliasPluginResolver(@NotNull KPMDaemon daemon)
    {
        this.aliasProvider = daemon.getAliasProvider();
    }

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        Alias alias = this.aliasProvider.getQueryByAlias(query.getQuery());

        if (alias == null)
            return new ErrorResult(this, ErrorResult.ErrorCause.PLUGIN_NOT_FOUND, ResolveResult.Source.LOCAL_KNOWN);

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
