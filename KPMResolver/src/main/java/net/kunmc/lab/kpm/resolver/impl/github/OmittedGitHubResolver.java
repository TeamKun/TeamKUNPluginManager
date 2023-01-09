package net.kunmc.lab.kpm.resolver.impl.github;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.interfaces.resolver.BaseResolver;
import net.kunmc.lab.kpm.interfaces.resolver.result.MultiResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.ErrorCause;
import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.resolver.result.AbstractSuccessResult;
import net.kunmc.lab.kpm.resolver.result.ErrorResultImpl;
import org.apache.commons.lang.StringUtils;

@AllArgsConstructor
public class OmittedGitHubResolver implements BaseResolver
{
    private static final String DELIMITER = "/";
    private final GitHubURLResolver gitHubURLResolver;

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        String repoName = query.getQuery();

        query.setQuery("https://github.com/" + repoName);

        ResolveResult res = this.gitHubURLResolver.resolve(query);

        if (res instanceof AbstractSuccessResult || res instanceof MultiResult)
            return res;

        query.setQuery(repoName); // restore query

        return new ErrorResultImpl(this, ErrorCause.PLUGIN_NOT_FOUND, ResolveResult.Source.GITHUB);
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
