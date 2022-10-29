package net.kunmc.lab.kpm.resolver;

import net.kunmc.lab.kpm.resolver.interfaces.BaseResolver;
import net.kunmc.lab.kpm.resolver.interfaces.URLResolver;
import net.kunmc.lab.kpm.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.resolver.result.PipeResult;
import net.kunmc.lab.kpm.resolver.result.ResolveResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * プラグインを解決するクラスです。
 */
public class PluginResolver
{
    private final HashMap<String, List<BaseResolver>> resolvers;
    private final List<BaseResolver> onNotFoundResolvers;
    private final List<BaseResolver> allResolvers;

    public PluginResolver()
    {
        this.resolvers = new HashMap<>();
        this.onNotFoundResolvers = new ArrayList<>();
        this.allResolvers = new ArrayList<>();
    }

    /**
     * リゾルバを追加します。
     *
     * @param resolver 追加するリゾルバ
     * @param names    リゾルバの名前とエイリアス
     */
    public void addResolver(BaseResolver resolver, String... names)
    {
        for (String name : names)
        {
            if (name.equalsIgnoreCase("http") || name.equalsIgnoreCase("https"))
                throw new IllegalArgumentException("HTTP and HTTPS are reserved names.");

            List<BaseResolver> resolverList = this.resolvers.get(name.toLowerCase());

            if (resolverList == null)
                this.resolvers.put(name.toLowerCase(), new ArrayList<>(Collections.singletonList(resolver)));
            else
                resolverList.add(resolver);

            this.allResolvers.add(resolver);
        }
    }

    /**
     * 代替リゾルバを追加します。
     * 代替リゾルバは、プラグインが見つからなかった場合にフォールバックとして使用されるリゾルバです。
     *
     * @param resolver 追加するリゾルバ
     */
    public void addOnNotFoundResolver(BaseResolver resolver)
    {
        this.onNotFoundResolvers.add(resolver);
    }

    /**
     * クエリを使用してプラグインを解決します。
     *
     * @param query クエリ
     */
    public ResolveResult resolve(String query)
    {
        QueryContext context = QueryContext.fromString(query);

        if (context.getResolverName() == null)
            return this.actuallyResolve(this.allResolvers, context);

        if (!this.resolvers.containsKey(context.getResolverName().toLowerCase()))
            return new ErrorResult(null, ErrorResult.ErrorCause.RESOLVER_MISMATCH, ResolveResult.Source.UNKNOWN);

        return this.actuallyResolve(this.resolvers.get(context.getResolverName().toLowerCase()), context);
    }

    private ResolveResult actuallyResolve(List<BaseResolver> resolvers, QueryContext queryContext)
    {
        URL url = null;

        String resolverName = queryContext.getResolverName();
        if (resolverName != null)
            url = toURL(queryContext.getQuery());

        ResolveResult result = this.resolves(resolvers, queryContext, url);


        if (result instanceof ErrorResult)
        {
            ErrorResult error = (ErrorResult) result;
            if (error.getCause() != ErrorResult.ErrorCause.VERSION_MISMATCH)
                result = this.resolves(this.onNotFoundResolvers, queryContext, url);
        }

        return result;
    }

    private ResolveResult resolves(List<BaseResolver> resolvers, QueryContext queryContext, URL url)
    {
        List<BaseResolver> finishedResolvers = new ArrayList<>();

        ErrorResult errorResult = new ErrorResult(null, ErrorResult.ErrorCause.RESOLVER_MISMATCH, ResolveResult.Source.UNKNOWN);
        for (BaseResolver resolver : resolvers)
        {
            if (finishedResolvers.contains(resolver))
                continue;

            finishedResolvers.add(resolver);

            ResolveResult result = this.actuallyResolve(resolver, queryContext, url);

            if (result == null || result instanceof PipeResult)
                continue;
            else if (result instanceof ErrorResult)
            {
                ErrorResult error = (ErrorResult) result;

                if (error.getCause() != ErrorResult.ErrorCause.PLUGIN_NOT_FOUND && error.getCause() != ErrorResult.ErrorCause.INVALID_QUERY)
                    return error;
                if (error.getCause() != ErrorResult.ErrorCause.RESOLVER_MISMATCH)
                    errorResult = (ErrorResult) result;

                continue;
            }

            return result;
        }

        return errorResult;
    }

    private ResolveResult actuallyResolve(BaseResolver resolver, QueryContext queryContext, URL url)
    {
        if (resolver instanceof URLResolver)
        {
            URLResolver urlResolver = (URLResolver) resolver;
            if (url != null && !isValidURLResolver(url, urlResolver))
                return null;
        }
        else if (!resolver.isValidResolver(queryContext))
            return null;

        return resolver.resolve(queryContext);
    }

    private static boolean isValidURLResolver(URL url, URLResolver resolver)
    {
        String[] hosts = resolver.getHosts();

        if (hosts.length == 0)
            return true;

        for (String host : hosts)
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
