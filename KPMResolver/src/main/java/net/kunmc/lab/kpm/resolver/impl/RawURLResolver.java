package net.kunmc.lab.kpm.resolver.impl;

import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.resolver.interfaces.URLResolver;
import net.kunmc.lab.kpm.resolver.result.MultiResult;
import net.kunmc.lab.kpm.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.result.SuccessResult;

public class RawURLResolver implements URLResolver
{

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        return new SuccessResult(this, query.getQuery(), ResolveResult.Source.DIRECT);
    }

    @Override
    public ResolveResult autoPickOnePlugin(MultiResult multiResult)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getHosts()
    {
        return new String[0];
    }
}
