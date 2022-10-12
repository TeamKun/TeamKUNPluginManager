package net.kunmc.lab.teamkunpluginmanager.resolver.result;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.resolver.QueryContext;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.BaseResolver;
import org.jetbrains.annotations.NotNull;

/**
 * 他のリゾルバに解決を委譲することを表す解決結果です。
 * 通常は内部でのみで使用され、{@link net.kunmc.lab.teamkunpluginmanager.resolver.PluginResolver#resolve(String)} から返されることはありません。
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
