package net.kunmc.lab.kpm.alias;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import net.kunmc.lab.kpm.db.ResultRow;
import net.kunmc.lab.kpm.db.Transaction;
import net.kunmc.lab.kpm.interfaces.alias.AliasProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.sql.Statement;

public class AliasProviderImpl implements AliasProvider
{
    @Getter(AccessLevel.PACKAGE)
    private final HikariDataSource db;

    public AliasProviderImpl(@NotNull Path path)
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
                            "alias TEXT NOT NULL PRIMARY KEY," +
                            "query TEXT NOT NULL," +
                            "source_id TEXT NOT NULL" +
                            ")");

                    stmt.execute("CREATE TABLE IF NOT EXISTS source(" +
                            "name TEXT PRIMARY KEY NOT NULL," +
                            "source TEXT NOT NULL," +
                            "type TEXT NOT NULL" +
                            ")");
                });
    }

    @Override
    public void close()
    {
        this.db.close();
    }

    @Override
    public AliasUpdaterImpl createUpdater(@NotNull String sourceName, @NotNull String sourceURL)
    {
        return new AliasUpdaterImpl(sourceName, sourceURL, this);
    }

    /**
     * エイリアスが存在するかどうかを返します。
     *
     * @param query エイリアス対象のkueri
     * @return エイリアスが存在するかどうか
     */
    @Override
    public boolean hasAlias(@NotNull String query)

    {
        return Transaction.create(this.db, "SELECT COUNT(*) FROM alias WHERE query = ?")
                .set(1, query)
                .isExists();
    }

    @Override
    public boolean hasSource(String id)
    {
        return Transaction.create(this.db, "SELECT COUNT(*) FROM source WHERE name = ?")
                .set(1, id)
                .isExists();
    }

    @Override
    public AliasSource getSource(String id)
    {
        try (Transaction transaction = Transaction.create(this.db, "SELECT * FROM source WHERE name = ?")
                .set(1, id);
             ResultRow row = transaction
                     .executeQuery()
                     .stream()
                     .findFirst().orElse(null))
        {
            if (row == null)
                return null;

            return new AliasSource(
                    row.getString("name"),
                    row.getString("source"),
                    AliasSourceType.valueOf(row.getString("type"))
            );
        }
    }

    @Override
    @Nullable
    public Alias getQueryByAlias(String alias)
    {
        try (Transaction transaction = Transaction.create(this.db, "SELECT * FROM alias WHERE alias = ?")
                .set(1, alias);
             ResultRow row = transaction
                     .executeQuery()
                     .stream()
                     .findFirst().orElse(null))
        {
            if (row == null)
                return null;

            return new Alias(
                    row.getString("alias"),
                    row.getString("query"),
                    row.getString("source_id")
            );
        }
    }

    @Override
    public int countAliases()
    {
        try (Transaction transaction = Transaction.create(this.db, "SELECT COUNT(alias) FROM alias"))
        {
            return transaction.executeQuery().stream()
                    .findFirst()
                    .map(row -> row.getInt("COUNT(alias)"))
                    .orElse(0);
        }
    }

}
