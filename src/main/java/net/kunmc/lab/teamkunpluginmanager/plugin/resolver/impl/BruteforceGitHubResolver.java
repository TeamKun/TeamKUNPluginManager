package net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl;

import lombok.AllArgsConstructor;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.QueryContext;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.interfaces.BaseResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.ResolveResult;

import java.util.List;

@AllArgsConstructor
public class BruteforceGitHubResolver implements BaseResolver
{
    private final Object gitHubName;
    private final GitHubURLResolver gitHubURLResolver;

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        Object obj = gitHubName;

        if (obj instanceof String) // Legacy support
        {
            query.setQuery("https://github.com/" + obj + "/" + query.getQuery());
            return gitHubURLResolver.resolve(query);
        }

        if (!(obj instanceof List) && !(obj instanceof String[]))
            throw new IllegalArgumentException("Invalid config: gitHubName: config must be a string or a list of strings");

        ResolveResult result = new ErrorResult(this, ErrorResult.ErrorCause.PLUGIN_NOT_FOUND, ResolveResult.Source.GITHUB);

        if ((obj instanceof List))
            return result;

        for (String str : (List<String>) obj)
        {

            query.setQuery("https://github.com/" + str + "/" + query.getQuery());
            result = gitHubURLResolver.resolve(query);

            if (result instanceof ErrorResult)
            {
                ErrorResult error = (ErrorResult) result;

                if (error.getCause() == ErrorResult.ErrorCause.INVALID_QUERY)
                    return new ErrorResult(this, ErrorResult.ErrorCause.PLUGIN_NOT_FOUND, ResolveResult.Source.GITHUB);

                if (error.getCause() != ErrorResult.ErrorCause.PLUGIN_NOT_FOUND)
                    return error;
                continue;
            }

            return result;
        }

        return result;
    }

    @Override
    public ResolveResult autoPickOnePlugin(MultiResult multiResult)
    {
        return null;
    }

    @Override
    public boolean isValidResolver(QueryContext query)
    {
        return true;
    }
}
