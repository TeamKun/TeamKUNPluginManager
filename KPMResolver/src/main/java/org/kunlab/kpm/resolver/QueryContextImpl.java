package org.kunlab.kpm.resolver;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.resolver.interfaces.QueryContext;
import org.kunlab.kpm.versioning.Version;

@Data
@Builder
class QueryContextImpl implements QueryContext
{
    @Nullable
    String resolverName;
    @NotNull
    String query;
    @Nullable
    Version version;

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        if (this.resolverName != null)
            sb.append(this.resolverName).append(resolverNameQuerySeparator);
        sb.append(this.query);
        if (this.version != null)
            sb.append(versionEqualQuerySeparator).append(this.version);

        return sb.toString();
    }
}
