package net.kunmc.lab.kpm.resolver.impl;

import net.kunmc.lab.kpm.interfaces.resolver.URLResolver;
import net.kunmc.lab.kpm.interfaces.resolver.result.MultiResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.resolver.result.AbstractSuccessResult;

public class RawURLResolver implements URLResolver
{

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        return new RawSuccessResult(query.getQuery());
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

    private class RawSuccessResult extends AbstractSuccessResult
    {
        public RawSuccessResult(String downloadUrl)
        {
            super(RawURLResolver.this, downloadUrl, null, null, ResolveResult.Source.DIRECT);
        }
    }
}
