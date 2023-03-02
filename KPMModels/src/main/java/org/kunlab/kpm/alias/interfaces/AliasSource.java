package org.kunlab.kpm.alias.interfaces;

import org.kunlab.kpm.alias.AliasSourceType;

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
