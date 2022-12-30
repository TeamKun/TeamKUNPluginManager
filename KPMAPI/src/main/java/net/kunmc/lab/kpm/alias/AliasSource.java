package net.kunmc.lab.kpm.alias;

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
    AliasSourceType type;
}
