package net.kunmc.lab.teamkunpluginmanager.plugin.resolver;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 複数の情報を含んだクエリ
 */
@Data
@Builder
public class QueryContext
{
    @Nullable
    String resolverName;
    @NotNull
    String query;
    @Nullable
    String version;

    private static final String resolverNameQuerySeparator = ">";
    private static final String versionEqualQuerySeparator = "==";

    /**
     * クエリ文字列からQueryContextをパースする
     *
     * @param query クエリ文字列
     * @return QueryContext
     */
    public static QueryContext fromString(@NotNull String query)
    {
        if (query.isEmpty())
            throw new IllegalArgumentException("Query cannot be empty");

        String[] queryParts = StringUtils.split(query, resolverNameQuerySeparator, 2);

        String resolverName = null;
        if (queryParts.length >= 2)
            resolverName = queryParts[0];

        String version = null;

        String part = queryParts[queryParts.length - 1];

        int versionSeparatorIndex = part.lastIndexOf(versionEqualQuerySeparator);
        if (versionSeparatorIndex != -1)
            version = part.substring(versionSeparatorIndex + 2);

        String plainQuery = part;
        if (version != null)
            plainQuery = part.substring(0, versionSeparatorIndex);

        return new QueryContext(resolverName, plainQuery, version);
    }
}