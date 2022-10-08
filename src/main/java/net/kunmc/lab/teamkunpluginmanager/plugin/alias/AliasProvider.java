package net.kunmc.lab.teamkunpluginmanager.plugin.alias;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.utils.ResultRow;
import net.kunmc.lab.teamkunpluginmanager.utils.Transaction;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.sql.Statement;

/**
 * プラグインのエイリアスを提供するクラスです。
 */
public class AliasProvider
{
    @Getter(AccessLevel.PACKAGE)
    private final HikariDataSource db;

    public AliasProvider(@NotNull Path path)
    {
        this.db = Transaction.createDataSource(path);

        this.initializeTables();
    }

    private void initializeTables()
    {
        Transaction.create(this.db)
                .doTransaction((tr) -> {
                    Statement stmt = tr.getConnection().createStatement();

                    stmt.execute("CREATE TABLE IF NOT EXISTS alias(" +
                            "name TEXT NOT NULL, " +
                            "alias TEXT NOT NULL PRIMARY KEY," +
                            "source_id TEXT NOT NULL" +
                            ")");

                    stmt.execute("CREATE TABLE IF NOT EXISTS source(" +
                            "name TEXT PRIMARY KEY NOT NULL, " +
                            "source TEXT NOT NULL," +
                            "type TEXT NOT NULL" +
                            ")");
                });
    }

    public void close()
    {
        this.db.close();
    }

    public AliasUpdater createUpdater(@NotNull String sourceName, @NotNull String sourceURL)
    {
        return new AliasUpdater(sourceName, sourceURL, this);
    }

    public boolean hasAlias(@NotNull String name)
    {
        return Transaction.create(this.db, "SELECT COUNT(*) FROM alias WHERE name = ?")
                .set(1, name)
                .isExists();
    }

    public boolean hasSource(String id)
    {
        return Transaction.create(this.db, "SELECT COUNT(*) FROM source WHERE name = ?")
                .set(1, id)
                .isExists();
    }

    public AliasSource getSource(int id)
    {
        try (ResultRow row = Transaction.create(this.db, "SELECT * FROM source WHERE name = ?")
                .set(1, id)
                .executeQuery()
                .stream()
                .findFirst().orElse(null))
        {
            if (row == null)
                return null;

            return new AliasSource(
                    row.getString("name"),
                    row.getString("source"),
                    AliasSource.SourceType.valueOf(row.getString("type"))
            );
        }
    }

    public Alias getAlias(String name)
    {
        try (ResultRow row = Transaction.create(this.db, "SELECT * FROM alias WHERE name = ?")
                .set(1, name)
                .executeQuery()
                .stream()
                .findFirst().orElse(null))
        {
            if (row == null)
                return null;

            return new Alias(
                    row.getString("name"),
                    row.getString("alias"),
                    row.getString("source_id")
            );
        }
    }


}
