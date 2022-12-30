package net.kunmc.lab.kpm.alias;

/**
 * ソースの種類を表す列挙型です。
 */
public enum AliasSourceType
{
    /**
     * Webサーバがソースです。
     */
    WEB_SERVER,
    /**
     * ローカルで定義されたソースです。
     */
    LOCAL_DEFINITIONS,
    /**
     * 動的に追加されたソースです。
     */
    DYNAMIC
}
