package org.kunlab.kpm.resolver.impl.github;

import lombok.AllArgsConstructor;
import org.kunlab.kpm.resolver.ErrorCause;
import org.kunlab.kpm.resolver.interfaces.BaseResolver;
import org.kunlab.kpm.resolver.interfaces.QueryContext;
import org.kunlab.kpm.resolver.interfaces.result.ErrorResult;
import org.kunlab.kpm.resolver.interfaces.result.MultiResult;
import org.kunlab.kpm.resolver.interfaces.result.ResolveResult;
import org.kunlab.kpm.resolver.result.ErrorResultImpl;

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
            query.setQuery(oldQuery);

            if (result instanceof ErrorResult)
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
