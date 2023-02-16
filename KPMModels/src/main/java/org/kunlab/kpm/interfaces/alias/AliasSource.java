package org.kunlab.kpm.interfaces.alias;

import org.kunlab.kpm.enums.alias.AliasSourceType;

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
