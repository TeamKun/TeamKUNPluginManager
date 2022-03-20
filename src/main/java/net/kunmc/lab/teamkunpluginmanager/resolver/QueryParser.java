package net.kunmc.lab.teamkunpluginmanager.resolver;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class QueryParser
{
    private static final String resolverNameQuerySeparator = ">";
    private static final String queryVersionSeparator = "=";

    public static QueryContext parseQuery(@NotNull String query)
    {
        if (query.isEmpty())
            throw new IllegalArgumentException("Query cannot be empty");

        String[] queryParts = StringUtils.split(query, resolverNameQuerySeparator, 2);

        String resolverName = null;
        if (queryParts.length >= 2)
            resolverName = queryParts[0];

        String version = null;

        String part = queryParts[queryParts.length - 1];

        int versionSeparatorIndex = part.lastIndexOf(queryVersionSeparator);
        if (versionSeparatorIndex != -1)
            version = part.substring(versionSeparatorIndex + 1);

        String plainQuery = part;
        if (version != null)
            plainQuery = part.substring(0, versionSeparatorIndex);

        return new QueryContext(resolverName, plainQuery, version);
    }
}
