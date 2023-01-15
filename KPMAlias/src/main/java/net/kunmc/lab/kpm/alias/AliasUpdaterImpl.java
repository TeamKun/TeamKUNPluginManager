package net.kunmc.lab.kpm.alias;

import lombok.Getter;
import net.kunmc.lab.kpm.db.Transaction;
import net.kunmc.lab.kpm.enums.alias.AliasSourceType;
import net.kunmc.lab.kpm.interfaces.alias.AliasUpdater;

public class AliasUpdaterImpl implements AliasUpdater
{
    private final String sourceName;
    private final String sourceURL;
    private final Transaction transaction;

    @Getter
    private long aliasesCount;

    public AliasUpdaterImpl(String sourceName, String sourceURL, AliasProviderImpl provider)
    {
        this.sourceName = sourceName;
        this.sourceURL = sourceURL;
        this.transaction = Transaction.create(provider.getDb());

        this.aliasesCount = 0;

        this.createCheckTable(sourceName);
    }

    private void createCheckTable(String sourceName)
    {
        this.transaction.renew("CREATE TABLE v_exists_alias AS SELECT alias FROM alias WHERE source_id = ?")
                .set(1, sourceName)
                .executeUpdate(false);
    }

    private void dropCheckTable()
    {
        this.transaction.renew("DROP TABLE IF EXISTS v_exists_alias")
                .executeUpdate(false);
    }

    @Override
    public void update(String alias, String query)
    {
        this.transaction.renew("INSERT OR REPLACE INTO alias (alias, query, source_id) VALUES (?, ?, ?)")
                .set(1, alias)
                .set(2, query)
                .set(3, this.sourceName)
                .executeUpdate(false);

        this.aliasesCount++;
    }

    private void deleteRemovedAlias()
    {
        this.transaction.renew("DELETE FROM alias WHERE alias IN " +
                        "(SELECT alias FROM v_exists_alias EXCEPT SELECT alias FROM alias WHERE source_id = ?)")
                .set(1, this.sourceName)
                .executeUpdate(false);
    }

    @Override
    public void done()
    {
        this.transaction.renew("INSERT OR REPLACE INTO source (name, source, type) VALUES (?, ?, ?)")
                .set(1, this.sourceName)
                .set(2, this.sourceURL)
                .set(3, AliasSourceType.WEB_SERVER.name())
                .executeUpdate(false);

        this.deleteRemovedAlias();
        this.dropCheckTable();
        this.transaction.finishManually();
    }

    @Override
    public void cancel()
    {
        this.transaction.abortManually();
    }
}
