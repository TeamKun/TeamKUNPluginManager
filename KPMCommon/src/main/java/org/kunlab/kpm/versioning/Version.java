package org.kunlab.kpm.versioning;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Objects;
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
                        "(?:\\.(?<patch>0|[1-9]\\d*))?" +
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
                preRelease == null ? new VersionElement("0"): new VersionElement(patch),
                preRelease == null ? null: new VersionElement(preRelease),
                buildMetadata == null ? null: new VersionElement(buildMetadata)
        );
    }

    public static boolean isValidVersionString(@NotNull String versionString)
    {
        return VERSION_PATTERN.matcher(versionString).find();
    }

    @NotNull
    public static Version of(@NotNull String rawVersion) throws InvalidVersionSyntaxException
    {
        return compileVersion(rawVersion);
    }

    @Nullable
    public static Version ofNullable(@NotNull String rawVersion)
    {
        try
        {
            return compileVersion(rawVersion);
        }
        catch (InvalidVersionSyntaxException e)
        {
            return null;
        }
    }

    public boolean isPreRelease()
    {
        return this.preRelease != null;
    }

    public boolean isNewerThan(@Nonnull Version other)
    {
        return this.compareTo(other) > 0;
    }

    public boolean isOlderThan(@Nonnull Version other)
    {
        return this.compareTo(other) < 0;
    }

    public boolean isNewerThanOrEqualTo(@Nonnull Version other)
    {
        return this.compareTo(other) >= 0;
    }

    public boolean isOlderThanOrEqualTo(@Nonnull Version other)
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

        // すべての要素が等しい場合、preReleaseとbuildMetadataを比較する

        if (!(this.preRelease == null && o.preRelease == null))
        {
            if (this.preRelease == null)
                return -1;
            else if (o.preRelease == null)
                return 1;
            else
                return this.preRelease.compareTo(o.preRelease);
        }

        if (!(this.buildMetadata == null && o.buildMetadata == null))
        {
            if (this.buildMetadata == null)
                return -1;
            else if (o.buildMetadata == null)
                return 1;
            else
                return this.buildMetadata.compareTo(o.buildMetadata);
        }

        return 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.rawVersion, this.major, this.minor, this.patch, this.preRelease, this.buildMetadata);
    }
}
