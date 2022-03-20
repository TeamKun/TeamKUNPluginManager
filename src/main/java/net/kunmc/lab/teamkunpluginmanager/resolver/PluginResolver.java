package net.kunmc.lab.teamkunpluginmanager.resolver;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.BaseResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.URLResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * プラグインを解決するクラス
 */
@Getter
@Setter
public class PluginResolver
{
    private final List<BaseResolver> resolvers;

    public PluginResolver()
    {
        this.resolvers = new ArrayList<>();
    }

    /**
     * クエリを使用してプラグインを解決する
     * @param query クエリ
     */
    public ResolveResult resolve(String query)
    {
        ResolveResult errorResult = new ErrorResult(ErrorResult.ErrorCause.PLUGIN_NOT_FOUND, ResolveResult.Source.UNKNOWN);

        URL url = toURL(query);

        for (BaseResolver resolver : resolvers)
        {
            if (resolver instanceof URLResolver)
            {
                URLResolver urlResolver = (URLResolver) resolver;
                if (url != null && !isValidURLResolver(url, urlResolver))
                    continue;
            }
            else if (!resolver.isValidResolver(query))
                continue;

            ResolveResult result = resolver.resolve(query);

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
