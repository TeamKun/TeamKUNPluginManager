package net.kunmc.lab.plugin.kpmupgrader.mocks;

import net.kunmc.lab.kpm.db.Transaction;
import net.kunmc.lab.kpm.enums.metadata.InstallOperator;
import net.kunmc.lab.kpm.interfaces.meta.PluginMetaIterator;
import net.kunmc.lab.kpm.interfaces.meta.PluginMetaProvider;
import net.kunmc.lab.kpm.meta.DependencyNode;
import net.kunmc.lab.kpm.meta.PluginMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class PluginMetaProviderMock implements PluginMetaProvider
{
    @Override
    public void close()
    {

    }

    @Override
    public List<DependencyNode> getDependOn(@NotNull String pluginName)
    {
        return Collections.emptyList();
    }

    @Override
    public List<DependencyNode> getSoftDependOn(@NotNull String pluginName)
    {
        return Collections.emptyList();
    }

    @Override
    public List<DependencyNode> getLoadBefore(@NotNull String pluginName)
    {
        return Collections.emptyList();
    }

    @Override
    public List<DependencyNode> getDependedBy(@NotNull String pluginName)
    {
        return Collections.emptyList();
    }

    @Override
    public List<DependencyNode> getSoftDependedBy(@NotNull String pluginName)
    {
        return Collections.emptyList();
    }

    @Override
    public List<DependencyNode> getLoadBeforeBy(@NotNull String pluginName)
    {
        return Collections.emptyList();
    }

    @Override
    public List<String> getAuthors(@NotNull String pluginName)
    {
        return Collections.emptyList();
    }

    @Override
    public List<String> getPluginsByAuthor(@NotNull String author)
    {
        return Collections.emptyList();
    }

    @Override
    public InstallOperator getInstalledBy(@NotNull String pluginName)
    {
        return InstallOperator.UNKNOWN;
    }

    @Override
    public boolean isDependency(@NotNull String pluginName)
    {
        return false;
    }

    @Override
    public void setDependencyFlag(@NotNull String pluginName, boolean isDependency)
    {

    }

    @Override
    public void updateResolveQuery(@NotNull String pluginName, @NotNull String query)
    {

    }

    @Override
    public boolean isPluginMetaExists(@NotNull String pluginName)
    {
        return false;
    }

    @Override
    public boolean isPluginMetaExists(@NotNull Plugin pluginName)
    {
        return false;
    }

    @Override
    public void savePluginMeta(@NotNull Plugin plugin, @NotNull InstallOperator installedBy, long installedAt, @Nullable String resolveQuery, boolean isDependency)
    {

    }

    @Override
    public void savePluginMeta(@NotNull PluginMeta meta)
    {

    }

    @Override
    public void removePluginMeta(@NotNull String pluginName)
    {

    }

    @Override
    public void removePluginMeta(String pluginName, Transaction transaction)
    {

    }

    @Override
    public void removePluginRelationalData(Connection connection, String pluginName) throws SQLException
    {

    }

    @Override
    public @NotNull PluginMeta getPluginMeta(@NotNull String pluginName, boolean includeDependencies, boolean includeAuthors)
    {
        return new PluginMeta(
                "Unknown",
                "Unknown",
                PluginLoadOrder.STARTUP,
                InstallOperator.UNKNOWN,
                false,
                null,
                -1,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    @Override
    public @NotNull PluginMeta getPluginMeta(@NotNull String pluginName)
    {
        return this.getPluginMeta(pluginName, false, false);
    }

    @Override
    public void saveDependencyTree(@NotNull List<DependencyNode> dependencyNodes)
    {

    }

    @Override
    public void buildDependencyTree(@NotNull Plugin plugin)
    {

    }

    @Override
    public void buildDependencyTree(@NotNull String pluginName)
    {

    }

    @Override
    public void deleteFromDependencyTree(@NotNull String pluginName)
    {

    }

    @Override
    public @NotNull PluginMetaIterator getPluginMetaIterator()
    {
        return new PluginMetaIterator()
        {
            @Override
            public boolean hasNext()
            {
                return false;
            }

            @Override
            public PluginMeta next()
            {
                return null;
            }

            @Override
            public void close()
            {

            }

            @Override
            public void remove()
            {

            }
        };
    }

    @Override
    public int countPlugins()
    {
        return 0;
    }

    @Override
    public List<String> getUnusedPlugins()
    {
        return Collections.emptyList();
    }
}
