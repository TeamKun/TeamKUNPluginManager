package net.kunmc.lab.kpm.alias;

import lombok.Value;

/**
 * プラグインのエイリアスです。
 */
@Value
public class Alias
{
    /**
     * エイリアスです。
     */
    String alias;
    /**
     * クエリです。
     */
    String query;
    /**
     * エイリアスのソースです。
     */
    String source;

}
