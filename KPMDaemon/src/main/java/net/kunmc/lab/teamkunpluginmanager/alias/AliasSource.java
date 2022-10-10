package net.kunmc.lab.teamkunpluginmanager.alias;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * エイリアスのソースを表すクラスです。
 */
@Value
public class AliasSource
{
    /**
     * 人間が読めるエイリアスの名前です。
     */
    @NotNull
    String name;
    /**
     * エイリアスのソースです。
     */
    @NotNull
    String source;

    /**
     * ソースの種類です。
     */
    @NotNull
    SourceType type;

    public enum SourceType
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
}
