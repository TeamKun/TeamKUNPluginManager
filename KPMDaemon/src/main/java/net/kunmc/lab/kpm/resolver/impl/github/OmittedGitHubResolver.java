package net.kunmc.lab.kpm.resolver.impl.github;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.resolver.interfaces.BaseResolver;
import net.kunmc.lab.kpm.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.resolver.result.MultiResult;
import net.kunmc.lab.kpm.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.result.SuccessResult;
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

        if (res instanceof SuccessResult || res instanceof MultiResult)
            return res;

        query.setQuery(repoName); // restore query

        return new ErrorResult(this, ErrorResult.ErrorCause.PLUGIN_NOT_FOUND, ResolveResult.Source.GITHUB);
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
