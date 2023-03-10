package org.kunlab.kpm.versioning;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VersionTest
{
    private static Version createVersion(String str)
    {
        return Version.of(str);
    }

    private static void testInvalidVersionSyntaxThrow(String str)
    {
        assertThrows(InvalidVersionSyntaxException.class, () -> createVersion(str));
        assertNull(Version.ofNullable(str));
    }

    private static void testNewer(Version v1, Version v2)
    {
        assertTrue(v1.compareTo(v2) > 0);
        assertTrue(v1.isNewerThan(v2));
    }

    private static void testOlder(Version v1, Version v2)
    {
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v1.isOlderThan(v2));
    }

    private static void testSame(Version v1, Version v2)
    {
        assertEquals(0, v1.compareTo(v2));
        assertTrue(v1.isEqualTo(v2));
    }

    @Test
    void 不正なバージョン構文の例外発生()
    {
        testInvalidVersionSyntaxThrow("1");
        testInvalidVersionSyntaxThrow("awdawdwada");
        testInvalidVersionSyntaxThrow("");
        testInvalidVersionSyntaxThrow("...");

        testInvalidVersionSyntaxThrow("-1.0.0");
        testInvalidVersionSyntaxThrow("1.-1.0");
        testInvalidVersionSyntaxThrow("1.0.-1");

        testInvalidVersionSyntaxThrow("1.0.0-");
        testInvalidVersionSyntaxThrow("1.0.0-pre+");
        testInvalidVersionSyntaxThrow("1.0.0-pre+build$");
    }

    @Test
    void 基本的なバージョンの比較ができるか()
    {
        Version basis = createVersion("1.0.0");
        Version test = createVersion("1.0.0");
        testSame(basis, test);

        test = createVersion("1.0.1");
        testOlder(basis, test);
        testNewer(test, basis);

        test = createVersion("1.1.0");
        testOlder(basis, test);
        testNewer(test, basis);

        test = createVersion("2.0.0");
        testOlder(basis, test);
        testNewer(test, basis);
    }

    @Test
    void preがつくほうが古い判定になるか()
    {
        Version basis = createVersion("1.0.0");
        Version test = createVersion("1.0.0-pre1");
        testOlder(basis, test);
        testNewer(test, basis);
    }

    @Test
    void 同じ基礎で違うpre同士の比較ができるか()
    {
        Version basis = createVersion("1.0.0-pre1");
        Version test = createVersion("1.0.0-pre1");
        testSame(basis, test);

        test = createVersion("1.0.0-pre2");
        testOlder(basis, test);
        testNewer(test, basis);
    }

    @Test
    void 違う基礎でpre同士の比較ができるか()
    {
        Version basis = createVersion("1.0.0-pre1");
        Version test = createVersion("1.0.1-pre2");
        testOlder(basis, test);
        testNewer(test, basis);

        test = createVersion("1.1.0-pre2");
        testOlder(basis, test);
        testNewer(test, basis);

        test = createVersion("1.1.0-pre1");
        testOlder(basis, test);
        testNewer(test, basis);
    }

    @Test
    void 同じ基礎で違うbuild同士の比較ができるか()
    {
        Version basis = createVersion("1.0.0+build1");
        Version test = createVersion("1.0.0+build1");
        testSame(basis, test);

        test = createVersion("1.0.0+build2");
        testOlder(basis, test);
        testNewer(test, basis);
    }

    @Test
    void 違う基礎で同じbuild同士の比較ができるか()
    {
        Version basis = createVersion("1.0.0+build1");
        Version test = createVersion("1.0.1+build1");
        testOlder(basis, test);
        testNewer(test, basis);

        test = createVersion("1.1.0+build1");
        testOlder(basis, test);
        testNewer(test, basis);

        test = createVersion("1.1.0+build2");
        testOlder(basis, test);
        testNewer(test, basis);
    }
}
