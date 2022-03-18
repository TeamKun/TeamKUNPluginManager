package net.kunmc.lab.teamkunpluginmanager.resolver;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.BaseResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import org.jetbrains.annotations.NotNull;

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
        ResolveResult errorResult = new ErrorResult(ErrorResult.ErrorCause.UNKNOWN_ERROR, ResolveResult.Source.DIRECT);

        for (BaseResolver resolver : resolvers)
        {
            if (!resolver.isValidResolver(query))
                continue;

            ResolveResult result = resolver.resolve(query);

            if (result instanceof ErrorResult)
            {
                errorResult = result;
                continue;
            }

            return result;
        }

        return errorResult;
    }
}
