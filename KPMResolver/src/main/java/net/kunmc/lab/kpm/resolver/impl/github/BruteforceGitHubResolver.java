package net.kunmc.lab.kpm.resolver.impl.github;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.interfaces.resolver.BaseResolver;
import net.kunmc.lab.kpm.interfaces.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.MultiResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.ErrorCause;
import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.resolver.result.ErrorResultImpl;

import java.util.List;

@AllArgsConstructor
public class BruteforceGitHubResolver implements BaseResolver
{
    private final List<String> gitHubName;
    private final GitHubURLResolver gitHubURLResolver;

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        ResolveResult result = new ErrorResultImpl(this, ErrorCause.PLUGIN_NOT_FOUND, ResolveResult.Source.GITHUB);
        String oldQuery = query.getQuery();

        for (String str : this.gitHubName)
        {
            query.setQuery("https://github.com/" + str + "/" + query.getQuery());
            result = this.gitHubURLResolver.resolve(query);

            if (result instanceof ErrorResultImpl)
            {
                ErrorResult error = (ErrorResult) result;

                if (error.getCause() == ErrorCause.INVALID_QUERY)
                {
                    result = new ErrorResultImpl(this, ErrorCause.PLUGIN_NOT_FOUND, ResolveResult.Source.GITHUB);
                    break;
                }
                else if (error.getCause() == ErrorCause.PLUGIN_NOT_FOUND)
                    continue;
                else
                    break;
            }

            return result;
        }

        // Result is always ErrorResult so we have to restore old query to pass it to next resolver.
        query.setQuery(oldQuery);

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
