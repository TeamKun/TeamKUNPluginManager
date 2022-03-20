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

        int versionSeparatorIndex = queryParts[1].lastIndexOf(queryVersionSeparator);
        if (versionSeparatorIndex != -1)
            version = queryParts[1].substring(versionSeparatorIndex + 1);

        String plainQuery = queryParts[1].substring(0, versionSeparatorIndex);

        return new QueryContext(resolverName, plainQuery, version);
    }
}
