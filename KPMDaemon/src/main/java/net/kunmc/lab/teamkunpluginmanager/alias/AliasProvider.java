package net.kunmc.lab.teamkunpluginmanager.alias;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.utils.db.ResultRow;
import net.kunmc.lab.teamkunpluginmanager.utils.db.Transaction;
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
     * @param name エイリアス対象の名前
     * @return エイリアスが存在するかどうか
     */
    public boolean hasAlias(@NotNull String name)
    {
        return Transaction.create(this.db, "SELECT COUNT(*) FROM alias WHERE name = ?")
                .set(1, name)
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

    /**
     * エイリアスを取得します。
     *
     * @param name エイリアス対象の名前
     * @return エイリアス
     */
    public Alias getAlias(String name)  // TODO: Update method name
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
