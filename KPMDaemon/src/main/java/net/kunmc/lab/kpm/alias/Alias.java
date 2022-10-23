package net.kunmc.lab.kpm.alias;

import lombok.Value;

/**
 * プラグインのエイリアスです。
 */
@Value
public class Alias
{
    /**
     * 正式名です。。
     */
    String name;

    /**
     * エイリアスです。
     */
    String alias;
    /**
     * エイリアスのソースです。
     */
    String source;

}
