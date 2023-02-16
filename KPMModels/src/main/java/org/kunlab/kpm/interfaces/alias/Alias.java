package org.kunlab.kpm.interfaces.alias;

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
