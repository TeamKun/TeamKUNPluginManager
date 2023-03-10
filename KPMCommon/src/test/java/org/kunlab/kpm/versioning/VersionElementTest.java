package org.kunlab.kpm.versioning;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VersionElementTest
{
    @Test
    void 数字のみのパース()
    {
        VersionElement element = new VersionElement("1");
        assertEquals(1, element.getIntValue());

        element = new VersionElement("123");
        assertEquals(123, element.getIntValue());
    }

    @Test
    void 文字のみのパース()
    {
        VersionElement element = new VersionElement("a");
        assertEquals(-1, element.getIntValue()); // 変換できない場合は-1が代入される。
        assertEquals("a", element.getRawValue());

        element = new VersionElement("abc");
        assertEquals(-1, element.getIntValue());
    }

    @Test
    void 数字と文字のパース()
    {
        VersionElement element = new VersionElement("1a");
        assertEquals(-1, element.getIntValue());
        assertEquals("1a", element.getRawValue());

        element = new VersionElement("123abc");
        assertEquals(-1, element.getIntValue());
    }

    @Test
    void 予約語と数字のパース()
    {
        VersionElement element = new VersionElement("snapshot-1");
        assertEquals(1, element.getIntValue());
        assertEquals("snapshot-1", element.getRawValue());

        element = new VersionElement("rc-123");
        assertEquals(123, element.getIntValue());
        assertEquals("rc-123", element.getRawValue());
    }

    @Test
    void 正常に比較できるか()
    {
        VersionElement element1 = new VersionElement("1");
        VersionElement element2 = new VersionElement("2");
        assertEquals(-1, element1.compareTo(element2));
        assertEquals(1, element2.compareTo(element1));

        element1 = new VersionElement("123abc");
        element2 = new VersionElement("123def");
        // Unicode順に比較される。
        assertEquals(-3, element1.compareTo(element2));
        assertEquals(3, element2.compareTo(element1));
    }
}
