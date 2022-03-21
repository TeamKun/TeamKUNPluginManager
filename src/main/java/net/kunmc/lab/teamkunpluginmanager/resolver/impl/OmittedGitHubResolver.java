package net.kunmc.lab.teamkunpluginmanager.resolver.impl;

import net.kunmc.lab.teamkunpluginmanager.resolver.QueryContext;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.BaseResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.PipeResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import org.apache.commons.lang.StringUtils;

public class OmittedGitHubResolver implements BaseResolver
{
    private static final String DELIMITER = "/";

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        query.setQuery("https://github.com/" + query.getQuery());
        return new PipeResult(query);
    }

    @Override
    public ResolveResult autoPickOnePlugin(MultiResult multiResult)
    {
        throw new UnsupportedOperationException("Why you call me?");
    }

    @Override
    public boolean isValidResolver(QueryContext query)
    {
        String[] split = StringUtils.split(query.getQuery(), DELIMITER);
        return split.length == 2 && !StringUtils.isEmpty(split[0]) && !StringUtils.isEmpty(split[1]);
    }
}
