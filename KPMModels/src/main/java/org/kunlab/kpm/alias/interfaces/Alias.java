package org.kunlab.kpm.alias.interfaces;

/**
 * プラグインのエイリアスです。
 */
public interface Alias
{
    /**
     * エイリアスです。
     */
    String getAlias();

    /**
     * クエリです。
     */
    String getQuery();

    /**
     * エイリアスのソースです。
     */
    String getSource();
}
