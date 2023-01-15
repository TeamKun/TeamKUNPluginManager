package net.kunmc.lab.kpm.interfaces.alias;

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
    net.kunmc.lab.kpm.alias.AliasSourceType getType();
}
