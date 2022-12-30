package net.kunmc.lab.plugin.kpmupgrader.migrator;

import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.versioning.Version;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface KPMMigrateAction
{
    void migrate(@NotNull KPMDaemon daemon, @NotNull Path kpmDataFolder);

    String getNeedMigrateVersionRange();

    default boolean isMigrateNeeded(@NotNull Version currentVersion, @NotNull Version targetVersion)
    {
        final String separator = "\\.\\.\\.";

        String[] range = this.getNeedMigrateVersionRange().split(separator);

        if (range.length > 2)
            throw new IllegalArgumentException("Invalid version range: " + this.getNeedMigrateVersionRange());

        String lowerBound = range[0];
        String upperBound = range.length == 2 ? range[1]: "";

        if (lowerBound.isEmpty() && upperBound.isEmpty())
            throw new IllegalArgumentException("Invalid version range: " + this.getNeedMigrateVersionRange());

        Version defFrom = Version.ofNullable(lowerBound);
        Version defTo = Version.ofNullable(upperBound);

        if (defFrom == null && defTo == null)
            throw new IllegalArgumentException("Invalid version range: " + this.getNeedMigrateVersionRange());

        boolean isClosed = defFrom != null && defTo != null;

        if (isClosed)
            return currentVersion.isNewerThanOrEqualTo(defFrom) && targetVersion.isOlderThanOrEqualTo(defTo);
        else if (defFrom != null)
            return currentVersion.isNewerThanOrEqualTo(defFrom);
        else
            return currentVersion.isOlderThanOrEqualTo(defTo);
    }

}
