package net.kunmc.lab.plugin.kpmupgrader.migrator;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.utils.versioning.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class KPMMigrator
{
    @Getter
    @Unmodifiable
    private static final List<KPMMigrateAction> MIGRATE_ACTIONS;

    static
    {
        MIGRATE_ACTIONS = new ArrayList<>();
    }

    public static void doMigrate(@NotNull KPMDaemon daemon, @NotNull Version fromVersion, @NotNull Version toVersion)
    {
        for (KPMMigrateAction action : MIGRATE_ACTIONS)
        {
            if (action.isMigrateNeeded(fromVersion, toVersion))
                action.migrate(daemon);
        }
    }
}
