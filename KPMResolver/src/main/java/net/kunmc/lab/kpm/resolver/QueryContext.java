package net.kunmc.lab.kpm.resolver;

import lombok.Builder;
import lombok.Data;
import net.kunmc.lab.kpm.versioning.Version;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 複数の情報を含んだクエリです。
 */
@Data
@Builder
public class QueryContext
{
    private static final String resolverNameQuerySeparator = ">";
    private static final String versionEqualQuerySeparator = "==";
    /**
     * 指定リゾルバの名前です。
     */
    @Nullable
    String resolverName;
    /**
     * 指定クエリです。
     */
    @NotNull
    String query;
    /**
     * 指定バージョンです。
     */
    @Nullable
    Version version;

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

        String[] queryParts = StringUtils.split(query, resolverNameQuerySeparator, 2);

        String resolverName = null;
        if (queryParts.length >= 2)
            resolverName = queryParts[0];

        String bodyAndVersion = queryParts[queryParts.length - 1];

        int versionSeparatorIndex = bodyAndVersion.lastIndexOf(versionEqualQuerySeparator);

        String versionStr = null;
        Version version = null;
        if (versionSeparatorIndex != -1)
        {
            versionStr = bodyAndVersion.substring(versionSeparatorIndex + 2);
            if (!Version.isValidVersionString(versionStr))
                throw new IllegalArgumentException("Invalid version string: " + versionStr);
            version = Version.of(versionStr);
        }

        String plainQuery = bodyAndVersion;
        if (versionStr != null)
            plainQuery = bodyAndVersion.substring(0, versionSeparatorIndex);

        return new QueryContext(resolverName, plainQuery, version);
    }

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
