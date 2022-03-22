package net.kunmc.lab.teamkunpluginmanager.resolver.result;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.resolver.QueryContext;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.BaseResolver;
import org.jetbrains.annotations.NotNull;

/**
 * 次のリゾルバに処理を任せる.
 */
@Value
public class PipeResult implements ResolveResult
{
    /**
     * リゾルバ
     */
    @NotNull
    BaseResolver resolver;
    /**
     * 改変したクエリ
     */
    @NotNull
    QueryContext query;
}
