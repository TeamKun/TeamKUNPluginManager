package net.kunmc.lab.teamkunpluginmanager.resolver.result;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * 次のリゾルバに処理を任せる.
 */
@Value
public class PipeResult implements ResolveResult
{
    @NotNull
    String query;
}
