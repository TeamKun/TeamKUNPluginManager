package net.kunmc.lab.kpm.versioning;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * バージョンの要素を表すクラスです。
 */
@Getter
public class VersionElement implements Comparable<VersionElement>
{
    /**
     * 生のバージョン要素です。
     */
    @NotNull
    private final String rawValue;

    /**
     * int型に変換されたバージョン要素です。
     * 変換できない場合は-1が代入されます。
     */
    private final int intValue;

    public VersionElement(@NotNull String rawValue)
    {
        this.rawValue = rawValue;

        int intValue;
        try
        {
            intValue = Integer.parseInt(rawValue);
        }
        catch (NumberFormatException e)
        {
            intValue = -1;
        }

        this.intValue = intValue;
    }

    @Override
    public int compareTo(@Nonnull VersionElement o)
    {
        if (this.intValue != -1 && o.intValue != -1)
            return Integer.compare(this.intValue, o.intValue);
        else
            return this.rawValue.compareTo(o.rawValue);
    }

    @Override
    public String toString()
    {
        return this.rawValue;
    }
}
