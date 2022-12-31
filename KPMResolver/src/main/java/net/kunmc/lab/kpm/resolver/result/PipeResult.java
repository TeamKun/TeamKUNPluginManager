package net.kunmc.lab.kpm.resolver.result;

import lombok.Value;
import net.kunmc.lab.kpm.interfaces.resolver.BaseResolver;
import net.kunmc.lab.kpm.interfaces.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.PluginResolverImpl;
import net.kunmc.lab.kpm.resolver.QueryContext;
import org.jetbrains.annotations.NotNull;

/**
 * 他のリゾルバに解決を委譲することを表す解決結果です。
 * 通常は内部でのみで使用され、{@link PluginResolverImpl#resolve(String)} から返されることはありません。
 */
@Value
public class PipeResult implements ResolveResult
{
    /**
     * この解決を提供したリゾルバです。
     */
    @NotNull
    BaseResolver resolver;
    /**
     * リゾルバがクエリを改変する場合に使用されるクエリです。
     */
    @NotNull
    QueryContext query;
}
