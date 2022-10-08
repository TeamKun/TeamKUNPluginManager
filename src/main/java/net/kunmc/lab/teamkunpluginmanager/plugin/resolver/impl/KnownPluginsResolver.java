package net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl;

import lombok.AllArgsConstructor;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.alias.Alias;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.PluginResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.QueryContext;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.interfaces.BaseResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.SuccessResult;

@AllArgsConstructor
public class KnownPluginsResolver implements BaseResolver
{
    private final PluginResolver resolver;

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        Alias alias = TeamKunPluginManager.getPlugin().getAliasProvider().getAlias(query.getQuery());

        if (alias == null)
            return new ErrorResult(this, ErrorResult.ErrorCause.PLUGIN_NOT_FOUND, ResolveResult.Source.LOCAL_KNOWN);

        try
        {
            ResolveResult detailedResult = resolver.resolve(alias.getName());
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
