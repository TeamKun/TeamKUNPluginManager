package net.kunmc.lab.kpm.resolver.impl;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.resolver.interfaces.BaseResolver;
import net.kunmc.lab.kpm.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.resolver.result.MultiResult;
import net.kunmc.lab.kpm.resolver.result.ResolveResult;

import java.util.List;

@AllArgsConstructor
public class BruteforceGitHubResolver implements BaseResolver
{
    private final List<String> gitHubName;
    private final GitHubURLResolver gitHubURLResolver;

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        ResolveResult result = new ErrorResult(this, ErrorResult.ErrorCause.PLUGIN_NOT_FOUND, ResolveResult.Source.GITHUB);

        for (String str : this.gitHubName)
        {
            String oldQuery = query.getQuery();
            query.setQuery("https://github.com/" + str + "/" + query.getQuery());
            result = this.gitHubURLResolver.resolve(query);

            if (result instanceof ErrorResult)
            {
                ErrorResult error = (ErrorResult) result;

                if (error.getCause() == ErrorResult.ErrorCause.INVALID_QUERY)
                {
                    query.setQuery(oldQuery);
                    return new ErrorResult(this, ErrorResult.ErrorCause.PLUGIN_NOT_FOUND, ResolveResult.Source.GITHUB);
                }
                if (error.getCause() != ErrorResult.ErrorCause.PLUGIN_NOT_FOUND)
                {
                    query.setQuery(oldQuery);
                    return result;
                }

                continue;
            }

            return result;
        }

        return result;
    }

    @Override
    public ResolveResult autoPickOnePlugin(MultiResult multiResult)
    {
        return this.gitHubURLResolver.autoPickOnePlugin(multiResult);
    }

    @Override
    public boolean isValidResolver(QueryContext query)
    {
        return true;
    }
}
