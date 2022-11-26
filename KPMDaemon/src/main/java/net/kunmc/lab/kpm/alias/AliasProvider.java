package net.kunmc.lab.kpm.alias;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import net.kunmc.lab.kpm.utils.db.ResultRow;
import net.kunmc.lab.kpm.utils.db.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /**
     * このクラスを破棄します。
     */
    public void close()
    {
        this.db.close();
    }

    /**
     * アップデータを作成します。
     *
     * @param sourceName ソースの名前
     * @param sourceURL  ソースのURL
     * @return アップデータ
     */
    public AliasUpdater createUpdater(@NotNull String sourceName, @NotNull String sourceURL)
    {
        return new AliasUpdater(sourceName, sourceURL, this);
    }

    /**
     * エイリアスが存在するかどうかを返します。
     *
     * @param query エイリアス対象のkueri
     * @return エイリアスが存在するかどうか
     */
    public boolean hasAlias(@NotNull String query)

    {
        return Transaction.create(this.db, "SELECT COUNT(*) FROM alias WHERE query = ?")
                .set(1, query)
                .isExists();
    }

    /**
     * ソースが存在するかどうかを返します。
     *
     * @param id ソースのID
     * @return ソースが存在するかどうか
     */
    public boolean hasSource(String id)
    {
        return Transaction.create(this.db, "SELECT COUNT(*) FROM source WHERE name = ?")
                .set(1, id)
                .isExists();
    }

    /**
     * ソースを取得します。
     *
     * @param id ソースのID
     * @return ソース
     */
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
                    AliasSource.SourceType.valueOf(row.getString("type"))
            );
        }
    }

    /**
     * エイリアスからクエリを取得します。
     *
     * @param alias エイリアス
     * @return 名前
     */
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


}
