package org.kunlab.kpm.resolver;

import org.kunlab.kpm.resolver.interfaces.BaseResolver;
import org.kunlab.kpm.resolver.interfaces.PluginResolver;
import org.kunlab.kpm.resolver.interfaces.URLResolver;
import org.kunlab.kpm.resolver.interfaces.result.ErrorResult;
import org.kunlab.kpm.resolver.interfaces.result.MultiResult;
import org.kunlab.kpm.resolver.interfaces.result.ResolveResult;
import org.kunlab.kpm.resolver.interfaces.result.SuccessResult;
import org.kunlab.kpm.resolver.result.ErrorResultImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * プラグインを解決するクラスです。
 */
public class PluginResolverImpl implements PluginResolver
{
    private final Map<String, List<BaseResolver>> resolvers;
    private final List<BaseResolver> fallbackResolvers;
    private final List<BaseResolver> allResolvers;

    public PluginResolverImpl()
    {
        this.resolvers = new HashMap<>();
        this.fallbackResolvers = new ArrayList<>();
        this.allResolvers = new ArrayList<>();
    }

    private static boolean isValidURLResolver(URL url, URLResolver resolver)
    {
        return Arrays.stream(resolver.getHosts()).parallel()
                .anyMatch(host -> url.getHost().equalsIgnoreCase(host));
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

    /**
     * リゾルバを追加します。
     *
     * @param resolver 追加するリゾルバ
     * @param names    リゾルバの名前とエイリアス
     */
    @Override
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
     * フォールバックリゾルバを追加します。
     * フォールバックリゾルバは、プラグインが見つからなかった場合にフォールバックとして使用されるリゾルバです。
     *
     * @param resolver 追加するリゾルバ
     */
    @Override
    public void addFallbackResolver(BaseResolver resolver)
    {
        this.fallbackResolvers.add(resolver);
    }

    /**
     * クエリを使用してプラグインを解決します。
     *
     * @param query クエリ
     */
    @Override
    public ResolveResult resolve(String query)
    {
        QueryContext context = QueryContext.fromString(query);

        if (context.getResolverName() == null)
            return this.actuallyResolve(this.allResolvers, context);

        if (!this.resolvers.containsKey(context.getResolverName().toLowerCase()))
            return new ErrorResultImpl(null, ErrorCause.RESOLVER_MISMATCH, ResolveResult.Source.UNKNOWN);

        return this.actuallyResolve(this.resolvers.get(context.getResolverName().toLowerCase()), context);
    }

    /**
     * 複数の結果({@link MultiResult})を一つの結果にピックアップします。
     *
     * @param multiResult 複数の結果
     * @return 一つの結果
     */
    @Override
    public ResolveResult pickUpOne(MultiResult multiResult)
    {
        return multiResult.getResolver().autoPickOnePlugin(multiResult);
    }

    private ResolveResult actuallyResolve(List<? extends BaseResolver> resolvers, QueryContext queryContext)
    {
        ResolveResult result = this.resolves(resolvers, queryContext);


        if (result instanceof ErrorResult)
        {
            ErrorResult error = (ErrorResult) result;
            if (error.getCause() == ErrorCause.RESOLVER_MISMATCH)
                result = this.resolves(this.fallbackResolvers, queryContext);
        }

        return result;
    }

    private ResolveResult resolves(List<? extends BaseResolver> resolvers, QueryContext queryContext)
    {
        List<BaseResolver> finishedResolvers = new ArrayList<>();

        ErrorResult errorResult = new ErrorResultImpl(null, ErrorCause.RESOLVER_MISMATCH, ResolveResult.Source.UNKNOWN);
        for (BaseResolver resolver : resolvers)
        {
            if (finishedResolvers.contains(resolver))
                continue;

            finishedResolvers.add(resolver);

            URL url = toURL(queryContext.getQuery());
            ResolveResult result = this.actuallyResolve(resolver, queryContext, url);

            if (result instanceof SuccessResult || result instanceof MultiResult)
                return result;
            else if (result instanceof ErrorResult)
            {
                ErrorResult error = (ErrorResult) result;

                if (error.getCause() != ErrorCause.RESOLVER_MISMATCH)
                {
                    errorResult = (ErrorResult) result;
                    if (error.getCause() != ErrorCause.PLUGIN_NOT_FOUND && error.getCause() != ErrorCause.INVALID_QUERY)
                        return error;
                }
            }
        }

        return errorResult;
    }

    private ResolveResult actuallyResolve(BaseResolver resolver, QueryContext queryContext, URL url)
    {
        if (resolver instanceof URLResolver)
        {
            if (url == null)
                return null;

            URLResolver urlResolver = (URLResolver) resolver;
            if (!isValidURLResolver(url, urlResolver))
                return null;
        }
        else if (!resolver.isValidResolver(queryContext))
            return null;

        return resolver.resolve(queryContext);
    }

}
