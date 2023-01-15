package net.kunmc.lab.kpm.interfaces.alias;

import net.kunmc.lab.kpm.enums.alias.AliasSourceType;

/**
 * エイリアスのソースを表すクラスです。
 */
public interface AliasSource
{
    /**
     * 人間が読めるエイリアスの名前です。
     */
    String getName();

    /**
     * エイリアスのソースです。
     */
    String getSource();

    /**
     * ソースの種類です。
     */
    AliasSourceType getType();
}
