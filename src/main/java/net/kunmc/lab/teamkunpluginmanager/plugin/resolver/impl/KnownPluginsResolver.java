package net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl;

import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPluginEntry;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.QueryContext;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.interfaces.BaseResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.SuccessResult;

public class KnownPluginsResolver implements BaseResolver
{
    @Override
    public ResolveResult resolve(QueryContext query)
    {
        KnownPluginEntry entry = KnownPlugins.getKnown(query.getQuery());

        if (entry == null)
            return new ErrorResult(this, ErrorResult.ErrorCause.PLUGIN_NOT_FOUND, ResolveResult.Source.LOCAL_KNOWN);

        return new SuccessResult(this, entry.getUrl(), null, null, ResolveResult.Source.LOCAL_KNOWN);
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