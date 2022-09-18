package net.kunmc.lab.teamkunpluginmanager.plugin.meta;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PluginMetaManager implements Listener
{
    private final HikariDataSource db;
    private final List<String> exceptedPluginModifications;

    public PluginMetaManager(@NotNull Plugin plugin, @NotNull Path databasePath)
    {
        this.db = createConnection(databasePath);
        this.exceptedPluginModifications = new ArrayList<>();

        this.initializeTables();

        // Below lambda will be executed after all plugins are loaded.
        // (Bukkit runs task after all plugins are loaded.)
        Runner.runLater(() -> Bukkit.getPluginManager().registerEvents(this, plugin), 1L);
    }

    private static String normalizePluginName(@NotNull String name)
    {
        return name.toLowerCase().replace(" ", "_");
    }

    public void close()
    {
        db.close();
    }

    public List<String> getDependOn(@NotNull String pluginName)
    {
        return getListFromTable("depend", normalizePluginName(pluginName), "name");
    }

    public List<String> getSoftDependOn(@NotNull String pluginName)
    {
        return getListFromTable("soft_depend", normalizePluginName(pluginName), "name");
    }

    public List<String> getLoadBefore(@NotNull String pluginName)
    {
        return getListFromTable("load_before", normalizePluginName(pluginName), "name");
    }

    public List<String> getDependedBy(@NotNull String pluginName)
    {
        return getListFromTable("dependency_tree", normalizePluginName(pluginName), "dependency");
    }

    public List<String> getSoftDependedBy(@NotNull String pluginName)
    {
        return getListFromTable("dependency_tree", normalizePluginName(pluginName), "soft_dependency");
    }

    public List<String> getLoadBeforeBy(@NotNull String pluginName)
    {
        return getListFromTable("dependency_tree", normalizePluginName(pluginName), "load_before");
    }

    public List<String> getAuthors(@NotNull String pluginName)
    {
        return getListFromTable("plugin_author", normalizePluginName(pluginName), "author");
    }

    public InstallOperator getInstalledBy(@NotNull String pluginName)
    {
        try (Connection con = db.getConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM meta WHERE name = ?");
            statement.setString(1, normalizePluginName(pluginName));

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next())
                return InstallOperator.OTHER;

            return InstallOperator.valueOf(resultSet.getString("installed_by"));
        }
        catch (SQLException e)
        {
            return InstallOperator.OTHER;
        }
    }

    public boolean isDependency(@NotNull String pluginName)
    {
        try (Connection con = db.getConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM meta WHERE name = ?");
            statement.setString(1, normalizePluginName(pluginName));

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next())
                return false;

            return resultSet.getBoolean("is_dependency");
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    public boolean setDependencyFlag(@NotNull String pluginName, boolean isDependency)
    {
        try (Connection con = db.getConnection())
        {
            PreparedStatement statement = con.prepareStatement("UPDATE meta SET is_dependency = ? WHERE name = ?");
            statement.setInt(1, isDependency ? 0: 1);
            statement.setString(2, normalizePluginName(pluginName));

            return statement.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    public void savePluginData(@NotNull Plugin plugin, boolean buildDependencyTree)
    {
        PluginDescriptionFile description = plugin.getDescription();

        String name = description.getName();
        String version = description.getVersion();
        List<String> authors = description.getAuthors();
        String loadTiming = description.getLoad().name();

        List<String> dependencies = description.getDepend();
        List<String> softDependencies = description.getSoftDepend();
        List<String> loadBefore = description.getLoadBefore();

        try (Connection connection = this.db.getConnection())
        {
            // Lock

            connection.createStatement().execute("BEGIN TRANSACTION");

            PreparedStatement statement =
                    connection.prepareStatement("INSERT INTO plugin(name, version, load_timing) VALUES(?, ?, ?)");
            statement.setString(1, name);
            statement.setString(2, version);
            statement.setString(3, loadTiming);
            statement.execute();

            statement = connection.prepareStatement("INSERT INTO plugin_author(name, author) VALUES(?, ?)");
            statement.setString(1, name);

            for (String author : authors)
            {
                statement.setString(2, author);
                statement.execute();
            }

            statement = connection.prepareStatement("INSERT INTO depend(name, dependency) VALUES(?, ?)");
            statement.setString(1, name);

            for (String dependency : dependencies)
            {
                statement.setString(2, dependency);
                statement.execute();
            }

            statement = connection.prepareStatement("INSERT INTO soft_depend(name, soft_dependency) VALUES(?, ?)");
            statement.setString(1, name);

            for (String softDependency : softDependencies)
            {
                statement.setString(2, softDependency);
                statement.execute();
            }

            statement = connection.prepareStatement("INSERT INTO load_before(name, load_before) VALUES(?, ?)");
            statement.setString(1, name);

            for (String load : loadBefore)
            {
                statement.setString(2, load);
                statement.execute();
            }

            connection.createStatement().execute("COMMIT TRANSACTION");

            if (buildDependencyTree)
                this.buildDependencyTree(plugin);
        }
        catch (SQLException e)
        {
            try (Connection connection = this.db.getConnection())
            {
                connection.createStatement().execute("ROLLBACK TRANSACTION");
            }
            catch (SQLException e1)
            {
                System.err.println("Failed to rollback transaction");
                throw new RuntimeException(e1);
            }
            throw new RuntimeException(e);
        }
    }

    public void savePluginMeta(@NotNull PluginMeta meta)
    {
        try (Connection connection = this.db.getConnection())
        {
            connection.createStatement().execute("BEGIN TRANSACTION");

            PreparedStatement statement =
                    connection.prepareStatement("INSERT INTO meta(name, installed_at, installed_by, resolve_query, is_dependency) VALUES(?, ?, ?, ?, ?)");
            statement.setString(1, meta.getName());
            statement.setLong(2, meta.getInstalledAt());
            statement.setString(3, meta.getInstalledBy().name());
            statement.setString(4, meta.getResolveQuery());
            statement.setInt(5, meta.isDependency() ? 1: 0);

            statement.execute();

            connection.createStatement().execute("COMMIT TRANSACTION");
        }
        catch (SQLException e)
        {
            try (Connection connection = this.db.getConnection())
            {
                connection.createStatement().execute("ROLLBACK TRANSACTION");
            }
            catch (SQLException e1)
            {
                System.err.println("Failed to rollback transaction");
                throw new RuntimeException(e1);
            }
            throw new RuntimeException(e);
        }
    }

    public void removePluginMeta(@NotNull String pluginName)
    {
        try (Connection connection = this.db.getConnection())
        {
            connection.createStatement().execute("BEGIN TRANSACTION");

            PreparedStatement statement =
                    connection.prepareStatement("DELETE FROM meta WHERE name = ?");
            statement.setString(1, pluginName);

            statement.execute();

            connection.createStatement().execute("COMMIT TRANSACTION");
        }
        catch (SQLException e)
        {
            try (Connection connection = this.db.getConnection())
            {
                connection.createStatement().execute("ROLLBACK TRANSACTION");
            }
            catch (SQLException e1)
            {
                System.err.println("Failed to rollback transaction");
                throw new RuntimeException(e1);
            }
            throw new RuntimeException(e);
        }
    }

    public void removePluginData(@NotNull String pluginName, boolean thinDependencyTree)
    {

        try (Connection connection = this.db.getConnection())
        {
            connection.createStatement().execute("BEGIN TRANSACTION");

            PreparedStatement statement =
                    connection.prepareStatement("DELETE FROM plugin_author WHERE name = ?");
            statement.setString(1, normalizePluginName(pluginName));
            statement.execute();

            statement = connection.prepareStatement("DELETE FROM depend WHERE name = ?");
            statement.setString(1, normalizePluginName(pluginName));
            statement.execute();

            statement = connection.prepareStatement("DELETE FROM soft_depend WHERE name = ?");
            statement.setString(1, normalizePluginName(pluginName));
            statement.execute();

            statement = connection.prepareStatement("DELETE FROM load_before WHERE name = ?");
            statement.setString(1, normalizePluginName(pluginName));
            statement.execute();

            statement = connection.prepareStatement("DELETE FROM plugin WHERE name = ?");
            statement.setString(1, normalizePluginName(pluginName));
            statement.execute();

            connection.createStatement().execute("COMMIT TRANSACTION");

            if (thinDependencyTree)
                this.thinDependencyTree(pluginName);
        }
        catch (SQLException e)
        {
            try (Connection connection = this.db.getConnection())
            {
                connection.createStatement().execute("ROLLBACK TRANSACTION");
            }
            catch (SQLException e1)
            {
                System.err.println("Failed to rollback transaction");
                throw new RuntimeException(e1);
            }
            throw new RuntimeException(e);
        }
    }

    public void onInstalled(@NotNull Plugin plugin, @NotNull InstallOperator operator, @Nullable String resolveQuery, long installedAt)
    {
        this.savePluginData(plugin, false);

        List<DependencyNode> dummy = Collections.emptyList();
        this.savePluginMeta(
                new PluginMeta(
                        normalizePluginName(plugin.getName()),
                        plugin.getDescription().getVersion(),
                        operator,
                        false, // Dummy value
                        resolveQuery,
                        installedAt,
                        dummy,
                        dummy
                )
        );
    }

    public void onInstalled(@NotNull Plugin plugin, @NotNull InstallOperator operator, @Nullable String resolveQuery)
    {
        onInstalled(plugin, operator, resolveQuery, System.currentTimeMillis());
    }

    public void onUninstalled(@NotNull String pluginName)
    {
        this.removePluginData(pluginName, false);
        this.removePluginMeta(pluginName);
    }

    public void preparePluginModify(@NotNull String pluginName)
    {
        this.exceptedPluginModifications.add(normalizePluginName(pluginName));
    }

    public void preparePluginModify(@NotNull Plugin plugin)
    {
        this.exceptedPluginModifications.add(normalizePluginName(plugin.getName()));
    }

    public void saveDependencyTree(@NotNull List<DependencyNode> dependencyNodes)
    {
        try (Connection connection = this.db.getConnection())
        {
            connection.createStatement().execute("BEGIN TRANSACTION");

            PreparedStatement statement =
                    connection.prepareStatement("INSERT INTO dependency_tree(name, parent, depend_type) VALUES(?, ?, ?)");

            for (DependencyNode node : dependencyNodes)
            {
                statement.setString(1, node.getPlugin());
                statement.setString(2, node.getDependsOn());
                statement.setString(3, node.getDependType().name());
                statement.execute();
            }

            connection.createStatement().execute("COMMIT TRANSACTION");
        }
        catch (SQLException e)
        {
            try (Connection connection = this.db.getConnection())
            {
                connection.createStatement().execute("ROLLBACK TRANSACTION");
            }
            catch (SQLException e1)
            {
                System.err.println("Failed to rollback transaction");
                throw new RuntimeException(e1);
            }
            throw new RuntimeException(e);
        }
    }

    public void buildDependencyTree(@NotNull Plugin plugin)
    {
        String pluginName = normalizePluginName(plugin.getName());

        List<String> dependencies = this.getDependOn(pluginName);
        List<String> softDependencies = this.getDependOn(pluginName);
        List<String> loadBefore = this.getLoadBeforeBy(pluginName);

        List<DependencyNode> dependencyNodes =
                this.createDependencyNodes(pluginName, dependencies, softDependencies, loadBefore);

        this.saveDependencyTree(dependencyNodes);
    }

    public void thinDependencyTree(@NotNull String pluginName)
    {
        try (Connection connection = this.db.getConnection())
        {
            connection.createStatement().execute("BEGIN TRANSACTION");

            PreparedStatement statement =
                    connection.prepareStatement("DELETE FROM dependency_tree WHERE name = ? OR parent = ?");
            statement.setString(1, pluginName);
            statement.setString(2, pluginName);
            statement.execute();

            connection.createStatement().execute("COMMIT TRANSACTION");
        }
        catch (SQLException e)
        {
            try (Connection connection = this.db.getConnection())
            {
                connection.createStatement().execute("ROLLBACK TRANSACTION");
            }
            catch (SQLException e1)
            {
                System.err.println("Failed to rollback transaction");
                throw new RuntimeException(e1);
            }
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event)
    {
        Plugin plugin = event.getPlugin();
        if (this.isNoAutoCreateMetadata(plugin))
            return;

        String pluginNameFull = PluginUtil.getPluginString(plugin);

        System.out.println("プラグインの追加が検出されました: " + pluginNameFull);

        System.out.println("プラグインのメタデータを作成してします ...");
        this.onInstalled(plugin, InstallOperator.SERVER_ADMIN, null);

        System.out.println("依存関係ツリーを構築しています ...");
        this.buildDependencyTree(plugin);

    }

    @EventHandler
    public void onDisable(PluginDisableEvent event)
    {
        Plugin plugin = event.getPlugin();
        if (this.isNoAutoCreateMetadata(plugin))
            return;

        String normalized = normalizePluginName(plugin.getName());
        if (this.exceptedPluginModifications.contains(normalized))
        {
            this.exceptedPluginModifications.remove(normalized);
            return;
        }

        String pluginNameFull = PluginUtil.getPluginString(plugin);

        System.out.println("プラグインの削除が検出されました: " + pluginNameFull);

        System.out.println("プラグインのメタデータを削除しています ...");
        this.onUninstalled(pluginNameFull);

        System.out.println("依存関係ツリーを構築しています ...");
        this.thinDependencyTree(pluginNameFull);
    }

    private boolean isNoAutoCreateMetadata(Plugin plugin)
    {
        String normalized = normalizePluginName(plugin.getName());
        if (this.exceptedPluginModifications.contains(normalized))
        {
            this.exceptedPluginModifications.remove(normalized);
            return true;
        }

        return false;
    }

    private List<DependencyNode> createDependencyNodes(String pluginName, List<String> dependencies, List<String> softDependencies, List<String> loadBefore)
    {
        List<DependencyNode> dependencyNodes = new ArrayList<>();
        List<String> processed = new ArrayList<>();

        for (String dependency : dependencies)
        {
            dependency = normalizePluginName(dependency);
            dependencyNodes.add(new DependencyNode(pluginName, dependency, DependType.HARD_DEPEND));
            processed.add(dependency);
        }

        for (String softDependency : softDependencies)
        {
            softDependency = normalizePluginName(softDependency);

            if (processed.contains(softDependency))
                continue;

            dependencyNodes.add(new DependencyNode(pluginName, softDependency, DependType.SOFT_DEPEND));
            processed.add(softDependency);
        }

        for (String load : loadBefore)
        {
            load = normalizePluginName(load);

            if (processed.contains(load))
                continue;

            dependencyNodes.add(new DependencyNode(pluginName, load, DependType.LOAD_BEFORE));
            processed.add(load);
        }

        return dependencyNodes;
    }

    private void initializeTables()
    {
        try (Connection con = db.getConnection())
        {
            Statement statement = con.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS plugin(" +
                    "name TEXT NOT NULL PRIMARY KEY, " +
                    "version TEXT NOT NULL," +
                    "load_timing TEXT NOT NULL" +
                    ")"
            );
            statement.execute("CREATE TABLE IF NOT EXISTS plugin_author(" +
                    "name TEXT NOT NULL UNIQUE," +
                    "author TEXT NOT NULL" +
                    ")"
            );

            statement.execute("CREATE TABLE IF NOT EXISTS meta(" +
                    "name TEXT NOT NULL PRIMARY KEY, " +
                    "installed_at INTEGER NOT NULL," +
                    "installed_by TEXT NOT NULL," +
                    "resolve_query TEXT," +
                    "is_dependency INTEGER(1) NOT NULL" +
                    ")"
            );

            statement.execute("CREATE TABLE IF NOT EXISTS depend(" +
                    "name TEXT NOT NULL," +
                    "dependency TEXT" +
                    ")"
            );
            statement.execute("CREATE TABLE IF NOT EXISTS soft_depend(" +
                    "name TEXT NOT NULL," +
                    "soft_dependency TEXT" +
                    ")"
            );
            statement.execute("CREATE TABLE IF NOT EXISTS load_before(" +
                    "name TEXT NOT NULL," +
                    "load_before TEXT" +
                    ")"
            );

            statement.execute("CREATE TABLE IF NOT EXISTS dependency_tree(" +
                    "name TEXT NOT NULL PRIMARY KEY," +
                    "parent TEXT," +
                    "depend_type NOT NULL" +
                    ")"
            );
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private HikariDataSource createConnection(@NotNull Path databasePath)
    {
        HikariConfig config = new HikariConfig();

        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + databasePath);

        config.setMaximumPoolSize(20);
        config.setLeakDetectionThreshold(300000);
        config.setAutoCommit(false);

        return new HikariDataSource(config);
    }

    @SuppressWarnings("SqlResolve")
    private List<String> getListFromTable(String tableName, String name, String field)
    {
        List<String> depends = new ArrayList<>();

        try (Connection con = db.getConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM " + tableName + " WHERE name = ?");
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
                depends.add(resultSet.getString(field));
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return depends;
    }

}
