package net.kunmc.lab.teamkunpluginmanager.resolver;

import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.BaseResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.URLResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * プラグインを解決するクラス
 */
public class PluginResolver
{
    private final HashMap<String, BaseResolver> resolvers; // TODO: LIST

    public PluginResolver()
    {
        this.resolvers = new HashMap<>();
    }

    public void addResolver(BaseResolver resolver, String... names)
    {
        for (String name : names)
        {
            if (name.equalsIgnoreCase("http") || name.equalsIgnoreCase("https"))
                throw new IllegalArgumentException("HTTP and HTTPS are reserved names.");

            resolvers.put(name.toLowerCase(), resolver);
        }
    }

    /**
     * クエリを使用してプラグインを解決する
     * @param query クエリ
     */
    public ResolveResult resolve(String query)
    {
        QueryContext context = QueryParser.parseQuery(query);

        if (context.getResolverName() == null)
            return actuallyResolve(new ArrayList<>(resolvers.values()), context);

        BaseResolver resolver = resolvers.get(context.getResolverName().toLowerCase());

        if (resolver == null || !resolver.isValidResolver(query))
            return new ErrorResult(ErrorResult.ErrorCause.RESOLVER_MISMATCH, ResolveResult.Source.UNKNOWN);

        return resolver.resolve(query);
    }

    private ResolveResult actuallyResolve(List<BaseResolver> resolvers, QueryContext queryContext)
    {
        URL url = null;

        String resolverName = queryContext.getResolverName();
        if (resolverName != null)
            url = toURL(queryContext.getQuery());

        ResolveResult errorResult = new ErrorResult(ErrorResult.ErrorCause.RESOLVER_MISMATCH, ResolveResult.Source.UNKNOWN);

        String queryString = queryContext.getQuery();

        for (BaseResolver resolver : resolvers)
        {
            if (resolver instanceof URLResolver)
            {
                URLResolver urlResolver = (URLResolver) resolver;
                if (url != null && !isValidURLResolver(url, urlResolver))
                    continue;
            }
            else if (!resolver.isValidResolver(queryString))
                continue;

            ResolveResult result = resolver.resolve(queryString);

            if (result instanceof ErrorResult)
            {
                ErrorResult error = (ErrorResult) result;

                if (error.getCause() != ErrorResult.ErrorCause.RESOLVER_MISMATCH)
                    errorResult = result;
                continue;
            }

            return result;
        }

        return errorResult;
    }

    private static boolean isValidURLResolver(URL url, URLResolver resolver)
    {
        for (String host: resolver.getHosts())
        {
            if (url.getHost().equalsIgnoreCase(host))
                return true;
        }

        return false;
    }

    private static URL toURL(String url)
    {
        try
        {
            return new URL(url);
        }
        catch (MalformedURLException e)
        {
            return null;
        }
    }

}
