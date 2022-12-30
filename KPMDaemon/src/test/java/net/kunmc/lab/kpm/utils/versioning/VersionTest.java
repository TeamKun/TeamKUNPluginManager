package net.kunmc.lab.kpm.utils.versioning;

import net.kunmc.lab.kpm.versioning.Version;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("NonAsciiCharacters")
public class VersionTest
{

    private static void testVersionEqual(int maj, int min, int pat, String pre, String build, Version version)
    {
        System.out.println("Testing:");
        System.out.println("    Major: " + maj);
        System.out.println("    Major (from Version): " + version.getMajor().getIntValue() + "(" + version.getMajor().getRawValue() + ")");
        System.out.println("    Minor: " + min);
        System.out.println("    Minor (from Version): " + version.getMinor().getIntValue() + "(" + version.getMinor().getRawValue() + ")");
        System.out.println("    Patch: " + pat);
        System.out.println("    Patch (from Version): " + version.getPatch().getIntValue() + "(" + version.getPatch().getRawValue() + ")");

        if (pre != null)
        {
            System.out.println("    Pre release: " + pre);
            System.out.println("    Pre release (from Version): " + version.getPreRelease());
            assertTrue(version.isPreRelease());
            // noinspection ConstantConditions
            assertEquals(pre, version.getPreRelease().getRawValue());
        }

        if (build != null)
        {
            System.out.println("    Build: " + build);
            assertNotNull(version.getBuildMetadata(), "Expected the version to have build metadata");
            System.out.println("    Build (from Version): " + version.getBuildMetadata().getRawValue());
            assertEquals(build, version.getBuildMetadata().getRawValue());
        }

        assertEquals(maj, version.getMajor().getIntValue());
        assertEquals(min, version.getMinor().getIntValue());
        assertEquals(pat, version.getPatch().getIntValue());

        System.out.println("==============");
    }

    private static void checkVersion(boolean isOlderExcepted, Version base, Version target)
    {
        if (isOlderExcepted)
        {
            assertTrue(base.isOlderThan(target));
            assertFalse(target.isOlderThan(base));
            assertTrue(target.isNewerThan(base));
            assertFalse(base.isNewerThan(target));
        }
        else
        {
            assertTrue(target.isOlderThan(base));
            assertFalse(base.isOlderThan(target));
            assertTrue(base.isNewerThan(target));
            assertFalse(target.isNewerThan(base));
        }
    }

    @Test
    void バージョンが正しくパースされるか()
    {
        for (int i = 0; i < 10; i++)
        {
            Random random = new Random();
            StringBuilder versionString = new StringBuilder();
            int maj = random.nextInt(100);
            int min = random.nextInt(100);
            int pat = random.nextInt(100);

            versionString.append(String.format("%d.%d.%d", maj, min, pat));

            String pre = null;
            if (random.nextBoolean())
            {
                pre = UUID.randomUUID().toString().replace("-", "");
                versionString.append("-").append(pre);
            }
            String build = null;
            if (random.nextBoolean())
            {
                build = UUID.randomUUID().toString().replace("-", "");
                versionString.append("+").append(build);
            }

            Version version = Version.of(versionString.toString());

            assertNotNull(version, "Expected the version to be non-null");

            testVersionEqual(maj, min, pat, pre, build, version);
        }
    }

    @Test
    void バージョン同士の大小比較ができるか()
    {
        for (int i = 0; i < 10; i++)
        {
            Random random = new Random();
            int seed1 = random.nextInt(100);
            int seed2 = random.nextInt(100);
            if (seed1 == seed2)
                seed2++;

            boolean isOlder = seed1 < seed2;

            Version version1 = Version.of(String.format("v%d.0.0", seed1));
            Version version2 = Version.of(String.format("v%d.0.0", seed2));
            checkVersion(isOlder, version1, version2);

            version1 = Version.of(String.format("v0.%d.0", seed1));
            version2 = Version.of(String.format("v0.%d.0", seed2));
            checkVersion(isOlder, version1, version2);

            version1 = Version.of(String.format("v0.0.%d", seed1));
            version2 = Version.of(String.format("v0.0.%d", seed2));
            checkVersion(isOlder, version1, version2);
        }
    }

    @Test
    void プレリリースが下方バージョンと判定されるか()
    {
        for (int i = 0; i < 10; i++)
        {

            Random random = new Random();
            StringBuilder versionString = new StringBuilder();
            StringBuilder lesserVersionString = new StringBuilder();
            int maj = random.nextInt(100);
            int min = random.nextInt(100);
            int pat = random.nextInt(100);

            Version version =
                    Version.of(versionString.append(String.format("%d.%d.%d", maj, min, pat)).toString());
            lesserVersionString.append(version);
            lesserVersionString.append("-").append(UUID.randomUUID().toString().replaceAll("-", ""));

            Version lesserAsVersion = Version.of(lesserVersionString.toString());

            assertEquals(-1, lesserAsVersion.compareTo(version));
            assertEquals(1, version.compareTo(lesserAsVersion));
        }
    }
}
