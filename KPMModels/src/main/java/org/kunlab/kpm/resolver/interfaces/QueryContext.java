package org.kunlab.kpm.resolver.interfaces;

import org.kunlab.kpm.versioning.Version;

/**
 * 依存関係の解決に使用される文字列（クエリ）のインタフェースです。
 */
public interface QueryContext
{
    /**
     * リゾルバ名とクエリの区切り文字です。
     */
    String resolverNameQuerySeparator = ">";
    /**
     * クエリとバージョンの区切り文字です。
     */
    String versionEqualQuerySeparator = "==";

    /**
     * 指定するリゾルバの名前を取得します。
     *
     * @return リゾルバの名前
     */
    String getResolverName();

    /**
     * 指定するリゾルバの名前を設定します。
     *
     * @param resolverName リゾルバの名前
     */
    void setResolverName(String resolverName);

    /**
     * 指定するクエリを取得します。
     *
     * @return クエリ
     */
    String getQuery();

    /**
     * 指定するクエリを設定します。
     *
     * @param query クエリ
     */
    void setQuery(String query);

    /**
     * 指定するバージョンを取得します。
     *
     * @return バージョン
     */
    Version getVersion();

    /**
     * 指定するバージョンを設定します。
     *
     * @param version バージョン
     */
    void setVersion(Version version);
}
