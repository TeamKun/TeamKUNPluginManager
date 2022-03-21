package net.kunmc.lab.teamkunpluginmanager.resolver;

import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.BaseResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.URLResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.PipeResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * プラグインを解決するクラス
 */
public class PluginResolver
{
    private final HashMap<String, List<BaseResolver>> resolvers;

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

            List<BaseResolver> resolverList = resolvers.get(name.toLowerCase());

            if (resolverList == null)
                resolvers.put(name.toLowerCase(), new ArrayList<>(Collections.singletonList(resolver)));
            else
                resolverList.add(resolver);
        }
    }

    /**
     * クエリを使用してプラグインを解決する
     * @param query クエリ
     */
    public ResolveResult resolve(String query)
    {
        QueryContext context = QueryContext.fromString(query);

        if (context.getResolverName() == null)
        {
            return actuallyResolve(resolvers.values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList()), context);
        }

        if (!resolvers.containsKey(context.getResolverName().toLowerCase()))
            return new ErrorResult(ErrorResult.ErrorCause.RESOLVER_MISMATCH, ResolveResult.Source.UNKNOWN);

        return actuallyResolve(resolvers.get(context.getResolverName().toLowerCase()), context);
    }

    private ResolveResult actuallyResolve(List<BaseResolver> resolvers, QueryContext queryContext)
    {
        URL url = null;

        List<BaseResolver> finishedResolvers = new ArrayList<>();

        String resolverName = queryContext.getResolverName();
        if (resolverName != null)
            url = toURL(queryContext.getQuery());

        ResolveResult errorResult = new ErrorResult(ErrorResult.ErrorCause.RESOLVER_MISMATCH, ResolveResult.Source.UNKNOWN);

        for (BaseResolver resolver : resolvers)
        {
            if (finishedResolvers.contains(resolver))
                continue;

            finishedResolvers.add(resolver);

            if (resolver instanceof URLResolver)
            {
                URLResolver urlResolver = (URLResolver) resolver;
                if (url != null && !isValidURLResolver(url, urlResolver))
                    continue;
            }
            else if (!resolver.isValidResolver(queryContext))
                continue;

            ResolveResult result = resolver.resolve(queryContext);

            if (result instanceof ErrorResult)
            {
                ErrorResult error = (ErrorResult) result;

                if (error.getCause() != ErrorResult.ErrorCause.PLUGIN_NOT_FOUND && error.getCause() != ErrorResult.ErrorCause.INVALID_QUERY)
                    return error;
                if (error.getCause() != ErrorResult.ErrorCause.RESOLVER_MISMATCH)
                    errorResult = result;

                continue;
            }
            else if (result instanceof PipeResult)
                continue;

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
