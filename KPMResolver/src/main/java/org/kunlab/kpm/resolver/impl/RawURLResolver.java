package org.kunlab.kpm.resolver.impl;

import org.kunlab.kpm.interfaces.resolver.URLResolver;
import org.kunlab.kpm.interfaces.resolver.result.MultiResult;
import org.kunlab.kpm.interfaces.resolver.result.ResolveResult;
import org.kunlab.kpm.resolver.QueryContext;
import org.kunlab.kpm.resolver.result.AbstractSuccessResult;

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
