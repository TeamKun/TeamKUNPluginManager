package org.kunlab.kpm.resolver.result;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.interfaces.resolver.BaseResolver;
import org.kunlab.kpm.interfaces.resolver.result.ResolveResult;
import org.kunlab.kpm.resolver.PluginResolverImpl;
import org.kunlab.kpm.resolver.QueryContext;

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
