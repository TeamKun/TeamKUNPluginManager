package net.kunmc.lab.teamkunpluginmanager.utils.versioning;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@AllArgsConstructor
public class Version implements Comparable<Version>
{
    private static final Pattern VERSION_PATTERN;

    static
    {
        VERSION_PATTERN = Pattern.compile(
                "^v?" +
                        "(?<major>0|[1-9]\\d*)" +
                        "\\.(?<minor>0|[1-9]\\d*)" +
                        "(?:\\.(?<patch>0|[1-9]\\d*))" +
                        "(?:-(?<preRelease>(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?" +
                        "(?:\\+(?<buildMetadata>[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");
    }

    @Getter(AccessLevel.NONE)
    @NotNull
    private final String rawVersion;
    @NotNull
    private final VersionElement major;
    @NotNull
    private final VersionElement minor;
    @NotNull
    private final VersionElement patch;
    @Nullable
    private final VersionElement preRelease;
    @Nullable
    private final VersionElement buildMetadata;

    private static Version compileVersion(String rawVersion) throws InvalidVersionSyntaxException
    {
        Matcher matcher = VERSION_PATTERN.matcher(rawVersion);

        if (!matcher.find())
            throw new InvalidVersionSyntaxException(rawVersion);

        String major = matcher.group("major");
        String minor = matcher.group("minor");
        String patch = matcher.group("patch");
        String preRelease = matcher.group("preRelease");
        String buildMetadata = matcher.group("buildMetadata");

        return new Version(
                rawVersion,
                new VersionElement(major),
                new VersionElement(minor),
                new VersionElement(patch),
                preRelease == null ? null: new VersionElement(preRelease),
                buildMetadata == null ? null: new VersionElement(buildMetadata)
        );
    }

    public static boolean isValidVersionString(@NotNull String versionString)
    {
        return VERSION_PATTERN.matcher(versionString).find();
    }

    @Nullable
    public static Version of(@NotNull String rawVersion)
    {
        if (rawVersion.isEmpty() || !isValidVersionString(rawVersion))
            return null;

        try
        {
            return compileVersion(rawVersion);
        }
        catch (InvalidVersionSyntaxException e)
        {
            return null;
        }
    }

    @NotNull
    public static Version ofUnsafe(@NotNull String rawVersion)
    {
        try
        {
            return compileVersion(rawVersion);
        }
        catch (InvalidVersionSyntaxException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean isPreRelease()
    {
        return this.preRelease != null;
    }

    public boolean isGreaterThan(@Nonnull Version other)
    {
        return this.compareTo(other) > 0;
    }

    public boolean isLessThan(@Nonnull Version other)
    {
        return this.compareTo(other) < 0;
    }

    public boolean isGreaterThanOrEqualTo(@Nonnull Version other)
    {
        return this.compareTo(other) >= 0;
    }

    public boolean isLessThanOrEqualTo(@Nonnull Version other)
    {
        return this.compareTo(other) <= 0;
    }

    public boolean isEqualTo(@Nonnull Version other)
    {
        return this.compareTo(other) == 0;
    }

    @Override
    public String toString()
    {
        return this.rawVersion;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (!(obj instanceof Version))
            return false;

        return this.isEqualTo((Version) obj);
    }

    @Override
    public int compareTo(@Nonnull Version o)
    {
        int majorCompare = this.major.compareTo(o.major);
        int minorCompare = this.minor.compareTo(o.minor);
        int patchCompare = this.patch.compareTo(o.patch);

        if (majorCompare != 0)
            return majorCompare;
        else if (minorCompare != 0)
            return minorCompare;
        else if (patchCompare != 0)
            return patchCompare;

        if (this.preRelease != null)
        {
            if (o.preRelease == null)
                return -1;
            else
                return this.preRelease.compareTo(o.preRelease);
        }
        else if (o.preRelease != null)
            return 1;

        if (this.buildMetadata != null)
        {
            if (o.buildMetadata == null)
                return 1;
            else
                return this.buildMetadata.compareTo(o.buildMetadata);
        }
        else if (o.buildMetadata != null)
            return -1;

        return 0;
    }
}
