package net.kunmc.lab.kpm.alias;

import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.resolver.PluginResolver;
import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.resolver.interfaces.BaseResolver;
import net.kunmc.lab.kpm.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.resolver.result.MultiResult;
import net.kunmc.lab.kpm.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.result.SuccessResult;
import org.jetbrains.annotations.NotNull;

/**
 * エイリアスを使用してプラグインを解決するクラスです。
 */
public class AliasPluginResolver implements BaseResolver
{
    private final PluginResolver resolver;
    private final AliasProvider aliasProvider;

    public AliasPluginResolver(@NotNull KPMDaemon daemon)
    {
        this.resolver = daemon.getPluginResolver();
        this.aliasProvider = daemon.getAliasProvider();
    }

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        Alias alias = this.aliasProvider.getNameByAlias(query.getQuery());

        if (alias == null)
            return new ErrorResult(this, ErrorResult.ErrorCause.PLUGIN_NOT_FOUND, ResolveResult.Source.LOCAL_KNOWN);

        try
        {
            ResolveResult detailedResult = this.resolver.resolve(alias.getAlias());
            if (detailedResult instanceof SuccessResult)
                return new SuccessResult(this, ((SuccessResult) detailedResult).getDownloadUrl(), ResolveResult.Source.LOCAL_KNOWN);
            else
                return detailedResult;
        }
        catch (StackOverflowError ignored)
        {
        }

        return new SuccessResult(this, alias.getName(), null, null, ResolveResult.Source.LOCAL_KNOWN);
    }

    @Override
    public ResolveResult autoPickOnePlugin(MultiResult multiResult)
    {
        throw new UnsupportedOperationException("Why you call me?");
    }

    @Override
    public boolean isValidResolver(QueryContext query)
    {
        return true;
    }
}
