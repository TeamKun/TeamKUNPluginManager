package net.kunmc.lab.teamkunpluginmanager.alias;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.utils.db.Transaction;

/**
 * エイリアスを更新するトランザクションを補助するクラスです。
 */
public class AliasUpdater
{
    private final String sourceName;
    private final String sourceURL;
    private final Transaction transaction;

    @Getter
    private long aliasesCount;

    public AliasUpdater(String sourceName, String sourceURL, AliasProvider provider)
    {
        this.sourceName = sourceName;
        this.sourceURL = sourceURL;
        this.transaction = Transaction.create(provider.getDb());

        this.aliasesCount = 0;

        this.createCheckTable(sourceName);
    }

    private void createCheckTable(String sourceName)
    {
        this.transaction.renew("CREATE TABLE v_exists_alias AS SELECT name FROM alias WHERE source_id = ?")
                .set(1, sourceName)
                .executeUpdate(false);
    }

    private void dropCheckTable()
    {
        this.transaction.renew("DROP TABLE IF EXISTS v_exists_alias")
                .executeUpdate(false);
    }

    /**
     * エイリアスのアップデートを行います。
     *
     * @param name  エイリアス対象の名前
     * @param alias エイリアス
     */
    public void update(String name, String alias)
    {
        this.transaction.renew("INSERT OR REPLACE INTO alias (name, alias, source_id) VALUES (?, ?, ?)")
                .set(1, name)
                .set(2, alias)
                .set(3, sourceName)
                .executeUpdate(false);

        this.aliasesCount++;
    }

    private void deleteRemovedAlias()
    {
        this.transaction.renew("DELETE FROM alias WHERE name IN " +
                        "(SELECT name FROM v_exists_alias EXCEPT SELECT name FROM alias WHERE source_id = ?)")
                .set(1, sourceName)
                .executeUpdate(false);
    }

    /**
     * すべてのアップデートを終了し、データベースの更新を行います。
     */
    public void done()
    {
        transaction.renew("INSERT OR REPLACE INTO source (name, source, type) VALUES (?, ?, ?)")
                .set(1, sourceName)
                .set(2, sourceURL)
                .set(3, AliasSource.SourceType.WEB_SERVER.name())
                .executeUpdate(false);

        this.deleteRemovedAlias();
        this.dropCheckTable();
        this.transaction.finishManually();
    }

    /**
     * アップデートをキャンセルし、データベースをロールバックします。
     */
    public void cancel()
    {
        this.transaction.abortManually();
    }
}
