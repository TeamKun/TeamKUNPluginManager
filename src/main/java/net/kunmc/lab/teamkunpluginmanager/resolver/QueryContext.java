package net.kunmc.lab.teamkunpluginmanager.resolver;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
class QueryContext
{
    @Nullable
    String resolverName;
    @NotNull
    String query;
    @Nullable
    String version;
}
