package org.kunlab.kpm.versioning;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Objects;

/**
 * バージョンの要素を表すクラスです。
 */
@Getter
public class VersionElement implements Comparable<VersionElement>
{
    private static final String[] reversedKeywords = new String[]{
            "snapshot", "alpha", "beta", "rc", "cr", "m", "preview", "pre"
    };

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
            intValue = Integer.parseInt(trimKeyword(rawValue));
        }
        catch (NumberFormatException e)
        {
            intValue = -1;
        }

        this.intValue = intValue;
    }

    private static String trimKeyword(String s)
    {
        String sLower = s.toLowerCase(Locale.ROOT);

        for (String keyword : reversedKeywords)
        {
            if (sLower.startsWith(keyword + "-"))
                return s.substring(keyword.length() + 1);
            else if (sLower.startsWith(keyword))
                return s.substring(keyword.length());
        }

        return s;
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof VersionElement))
            return false;
        VersionElement that = (VersionElement) o;
        return this.intValue == that.intValue && this.rawValue.equals(that.rawValue);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.rawValue, this.intValue);
    }
}
