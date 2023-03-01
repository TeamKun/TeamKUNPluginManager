package org.kunlab.kpm.alias.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * プラグインのエイリアスを提供するクラスです。
 */
public interface AliasProvider
{
    /**
     * このクラスを破棄します。
     */
    void close();

    /**
     * アップデータを作成します。
     *
     * @param sourceName ソースの名前
     * @param sourceURL  ソースのURL
     * @return アップデータ
     */
    AliasUpdater createUpdater(@NotNull String sourceName, @NotNull String sourceURL);

    /**
     * エイリアスが存在するかどうかを返します。
     *
     * @param query エイリアス対象のkueri
     * @return エイリアスが存在するかどうか
     */
    boolean hasAlias(@NotNull String query);

    /**
     * ソースが存在するかどうかを返します。
     *
     * @param id ソースのID
     * @return ソースが存在するかどうか
     */
    boolean hasSource(String id);

    /**
     * ソースを取得します。
     *
     * @param id ソースのID
     * @return ソース
     */
    AliasSource getSource(String id);

    /**
     * エイリアスからクエリを取得します。
     *
     * @param alias エイリアス
     * @return 名前
     */
    @Nullable Alias getQueryByAlias(String alias);

    /**
     * エイリアスの数を取得します。
     *
     * @return エイリアスの数
     */
    int countAliases();
}
