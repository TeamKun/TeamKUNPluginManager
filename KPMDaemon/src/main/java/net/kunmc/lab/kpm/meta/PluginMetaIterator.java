package net.kunmc.lab.kpm.meta;

import net.kunmc.lab.kpm.utils.db.Transaction;
import org.bukkit.plugin.PluginLoadOrder;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PluginMetaIterator implements Iterator<PluginMeta>, AutoCloseable
{
    private final PluginMetaProvider provider;
    private final Transaction transaction;
    private final Transaction.QueryResult<PluginMeta> results;
    private final List<String> removeTargets;

    private boolean hasNextCalled;
    private boolean lastHasNextResult;

    public PluginMetaIterator(@NotNull PluginMetaProvider metaProvider, @NotNull Transaction transaction)
    {
        this.provider = metaProvider;
        this.transaction = transaction.renew("SELECT * FROM plugin_meta");
        this.results = this.transaction.<PluginMeta>executeQuery()
                .setMapper(row -> {
                    String name = row.getString("name");
                    String version = row.getString("version");
                    PluginLoadOrder loadTiming = PluginLoadOrder.valueOf(row.getString("load_timing"));
                    long installedAt = row.getLong("installed_at");
                    InstallOperator installedBy = InstallOperator.valueOf(row.getString("installed_by"));
                    String resolveQuery = row.getString("resolve_query");
                    boolean isDependency = row.getInt("is_dependency") == 1;

                    List<String> authors = metaProvider.getAuthors(name);

                    List<DependencyNode> dependedBy = metaProvider.getDependedBy(name);
                    List<DependencyNode> dependsOn = metaProvider.getDependOn(name);

                    dependedBy.addAll(metaProvider.getSoftDependedBy(name));
                    dependsOn.addAll(metaProvider.getSoftDependOn(name));

                    dependedBy.addAll(metaProvider.getLoadBeforeBy(name));
                    dependsOn.addAll(metaProvider.getLoadBefore(name));

                    return new PluginMeta(
                            name,
                            version,
                            loadTiming,
                            installedBy,
                            isDependency,
                            resolveQuery,
                            installedAt,
                            authors,
                            dependedBy,
                            dependsOn
                    );
                });
        this.removeTargets = new ArrayList<>();

        this.hasNextCalled = false;
    }

    @Override
    public boolean hasNext()
    {
        this.hasNextCalled = true;
        return this.lastHasNextResult = this.results.next();
    }

    @Override
    public PluginMeta next()
    {
        if (this.hasNextCalled)
        {
            this.hasNextCalled = false;
            return this.lastHasNextResult ? this.results.get(): null;
        }

        return this.results.next() ? this.results.get(): null;
    }

    @Override
    public void close()
    {
        try
        {
            this.results.close();

            for (String name : this.removeTargets)
                this.provider.removePluginRelationalData(this.transaction.getConnection(), name);

            this.transaction.finishManually();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            this.transaction.finishManually();
        }
    }

    @Override
    public void remove()
    {
        PluginMeta meta = this.results.get();
        if (meta == null)
            throw new IllegalStateException("next() has not been called yet");

        this.removeTargets.add(meta.getName());
    }
}
