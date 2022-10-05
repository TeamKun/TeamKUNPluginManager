package net.kunmc.lab.teamkunpluginmanager.plugin.alias;

import com.zaxxer.hikari.HikariDataSource;
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

    public void addAlias(@NotNull String name, @NotNull String alias, @NotNull String sourceId)
    {
        Transaction.create(this.db, "INSERT INTO alias(name, alias, source_id) VALUES(?, ?, ?)")
                .set(1, name)
                .set(2, alias)
                .set(3, sourceId)
                .executeUpdate(true);
    }

    public void addSource(@NotNull String name, @NotNull String source, @NotNull AliasSource.SourceType type)
    {

        if (hasSource(name))
            throw new IllegalArgumentException("The source already exists.");

        Transaction.create(this.db, "INSERT INTO source(name, source, type) VALUES(?, ?, ?)")
                .set(1, name)
                .set(2, source)
                .set(3, type.name())
                .executeUpdate(true);
    }

    public void removeAlias(@NotNull String name)
    {
        Transaction.create(this.db, "DELETE FROM alias WHERE name = ?")
                .set(1, name)
                .executeUpdate();
    }

    public void removeSource(String id)
    {
        Transaction.create(this.db, "DELETE FROM source WHERE name = ?")
                .set(1, id)
                .executeUpdate();
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
