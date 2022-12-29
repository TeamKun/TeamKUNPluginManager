package net.kunmc.lab.plugin.kpmupgrader.migrator;

import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.utils.versioning.Version;
import org.jetbrains.annotations.NotNull;

public interface KPMMigrateAction
{
    void migrate(@NotNull KPMDaemon daemon);
    
    String getNeedMigrateVersionRange();

    default boolean isMigrateNeeded(@NotNull Version currentVersion, @NotNull Version targetVersion)
    {
        final String separator = "...";

        String[] range = this.getNeedMigrateVersionRange().split(separator);

        if (range.length != 2)
            throw new IllegalArgumentException("Invalid version range: " + this.getNeedMigrateVersionRange());

        String lowerBound = range[0];
        String upperBound = range[1];

        if (lowerBound.isEmpty() && upperBound.isEmpty())
            throw new IllegalArgumentException("Invalid version range: " + this.getNeedMigrateVersionRange());

        Version defFrom = Version.ofNullable(lowerBound);
        Version defTo = Version.ofNullable(upperBound);

        if (defFrom == null && defTo == null)
            throw new IllegalArgumentException("Invalid version range: " + this.getNeedMigrateVersionRange());

        boolean isClosed = defFrom != null && defTo != null;

        if (isClosed)
            return defFrom.isNewerThanOrEqualTo(currentVersion) && defTo.isOlderThan(targetVersion);
        else if (defFrom != null)
            return defFrom.isNewerThanOrEqualTo(currentVersion);
        return defTo.isOlderThan(targetVersion);
    }

}
