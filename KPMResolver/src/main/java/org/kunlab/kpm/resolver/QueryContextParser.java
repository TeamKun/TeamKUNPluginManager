package org.kunlab.kpm.resolver;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.resolver.interfaces.QueryContext;
import org.kunlab.kpm.versioning.Version;

public class QueryContextParser
{

    /**
     * クエリ文字列からQueryContextを生成します。
     *
     * @param query クエリ文字列
     * @return QueryContext
     */
    public static QueryContext fromString(@NotNull String query)
    {
        if (query.isEmpty())
            throw new IllegalArgumentException("Query cannot be empty");

        String[] queryParts = StringUtils.split(query, QueryContext.resolverNameQuerySeparator, 2);

        String resolverName = null;
        if (queryParts.length >= 2)
            resolverName = queryParts[0];

        String bodyAndVersion = queryParts[queryParts.length - 1];

        int versionSeparatorIndex = bodyAndVersion.lastIndexOf(QueryContext.versionEqualQuerySeparator);

        String versionStr = null;
        Version version = null;
        boolean chooseVersion = false;
        if (versionSeparatorIndex != -1)
        {
            versionStr = bodyAndVersion.substring(versionSeparatorIndex + 2);
            if (versionStr.equals(QueryContext.versionChooseSpecifier))
                chooseVersion = true;
            else
            {
                if (!Version.isValidVersionString(versionStr))
                    throw new IllegalArgumentException("Invalid version string: " + versionStr);
                version = Version.of(versionStr);
            }
        }

        String plainQuery = bodyAndVersion;
        if (versionStr != null)
            plainQuery = bodyAndVersion.substring(0, versionSeparatorIndex);

        return new QueryContextImpl(resolverName, plainQuery, version, chooseVersion);
    }
}
