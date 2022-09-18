package net.kunmc.lab.teamkunpluginmanager.plugin.meta;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * プラグインのメタデータを提供するクラスです。
 */
public class PluginMetaProvider implements Listener
{
    private final HikariDataSource db;

    public PluginMetaProvider(@NotNull Plugin plugin, @NotNull Path databasePath)
    {
        this.db = createConnection(databasePath);

        this.initializeTables();

        // Below lambda will be executed after all plugins are loaded.
        // (Bukkit runs task after all plugins are loaded.)
        Runner.runLater(() -> Bukkit.getPluginManager().registerEvents(this, plugin), 1L);
    }

    private static List<DependencyNode> createDependencyNodes(String pluginName, List<String> dependencies, List<String> softDependencies, List<String> loadBefore)
    {
        List<DependencyNode> dependencyNodes = new ArrayList<>();
        List<String> processed = new ArrayList<>();

        for (String dependency : dependencies)
        {
            dependencyNodes.add(new DependencyNode(pluginName, dependency, DependType.HARD_DEPEND));
            processed.add(dependency);
        }

        for (String softDependency : softDependencies)
        {
            if (processed.contains(softDependency))
                continue;

            dependencyNodes.add(new DependencyNode(pluginName, softDependency, DependType.SOFT_DEPEND));
            processed.add(softDependency);
        }

        for (String load : loadBefore)
        {
            if (processed.contains(load))
                continue;

            dependencyNodes.add(new DependencyNode(pluginName, load, DependType.LOAD_BEFORE));
            processed.add(load);
        }

        return dependencyNodes;
    }

    /**
     * このクラスを破棄します。
     */
    public void close()
    {
        db.close();
    }

    /**
     * プラグインが依存(depend)しているプラグインのリストを取得します。
     *
     * @param pluginName プラグインの名前
     * @return 依存しているプラグインのリスト
     */
    public List<String> getDependOn(@NotNull String pluginName)
    {
        return getListFromTable("depend", pluginName, "name");
    }

    /**
     * プラグインが依存(soft_depend)しているプラグインのリストを取得します。
     *
     * @param pluginName プラグインの名前
     * @return 依存しているプラグインのリスト
     */
    public List<String> getSoftDependOn(@NotNull String pluginName)
    {
        return getListFromTable("soft_depend", pluginName, "name");
    }

    /**
     * プラグインを依存(load_before)しているプラグインのリストを取得します。
     * load_before は特殊な依存で, 依存しているプラグインを先に読み込むようにします。
     *
     * @param pluginName プラグインの名前
     * @return 依存しているプラグインのリスト
     */
    public List<String> getLoadBefore(@NotNull String pluginName)
    {
        return getListFromTable("load_before", pluginName, "name");
    }

    /**
     * プラグインが依存(depend)されているプラグインのリストを取得します。
     *
     * @param pluginName プラグインの名前
     * @return 依存されているプラグインのリスト
     */
    public List<String> getDependedBy(@NotNull String pluginName)
    {
        return getListFromTable("dependency_tree", pluginName, "dependency");
    }

    /**
     * プラグインが依存(soft_depend)されているプラグインのリストを取得します。
     *
     * @param pluginName プラグインの名前
     * @return 依存されているプラグインのリスト
     */
    public List<String> getSoftDependedBy(@NotNull String pluginName)
    {
        return getListFromTable("dependency_tree", pluginName, "soft_dependency");
    }

    /**
     * プラグインが依存(load_before)されているプラグインのリストを取得します。
     * load_before は特殊な依存で, 依存しているプラグインを先に読み込むようにします。
     *
     * @param pluginName プラグインの名前
     * @return 依存されているプラグインのリスト
     */
    public List<String> getLoadBeforeBy(@NotNull String pluginName)
    {
        return getListFromTable("dependency_tree", pluginName, "load_before");
    }

    /**
     * プラグインの作者を取得します。
     *
     * @param pluginName プラグインの名前
     * @return プラグインの作者
     */
    public List<String> getAuthors(@NotNull String pluginName)
    {
        return getListFromTable("plugin_author", pluginName, "author");
    }

    /**
     * プラグインの作者からプラグインのリストを取得します。
     *
     * @param author プラグインの作者
     * @return プラグインのリスト
     */
    public List<String> getPluginsByAuthor(@NotNull String author)
    {
        return getListFromTable("plugin_author", author, "author", "name");
    }

    /**
     * プラグインが誰によってインストールされたかを取得します。
     *
     * @param pluginName プラグインの名前
     * @return プラグインのインストール者
     */
    public InstallOperator getInstalledBy(@NotNull String pluginName)
    {
        try (Connection con = this.db.getConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM meta WHERE name = ?");
            statement.setString(1, pluginName);

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

    /**
     * プラグインが依存関係かどうかを取得します。
     * 依存関係としてマークされている場合, 自動削除等の機能の対象になります。
     *
     * @param pluginName プラグインの名前
     * @return 依存関係かどうか
     */
    public boolean isDependency(@NotNull String pluginName)
    {
        try (Connection con = this.db.getConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM meta WHERE name = ?");
            statement.setString(1, pluginName);

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

    /**
     * プラグインが依存関係かどうかを設定します。
     *
     * @param pluginName   プラグインの名前
     * @param isDependency 依存関係かどうか
     */
    public void setDependencyFlag(@NotNull String pluginName, boolean isDependency)
    {
        try (Connection con = this.db.getConnection())
        {
            con.createStatement().execute("BEGIN TRANSACTION");
            PreparedStatement statement = con.prepareStatement("UPDATE meta SET is_dependency = ? WHERE name = ?");
            statement.setInt(1, isDependency ? 0: 1);
            statement.setString(2, pluginName);

            statement.executeUpdate();

            con.createStatement().execute("COMMIT TRANSACTION");
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

    /**
     * プラグインデータを保存します。
     *
     * @param plugin              プラグイン
     * @param buildDependencyTree 依存関係ツリーを構築するかどうか
     */
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

    /**
     * プラグインのメタデータを保存します。
     *
     * @param meta メタデータ
     */
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

    /**
     * プラグインのメタデータを削除します。
     *
     * @param pluginName プラグインの名前
     */
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

    /**
     * プラグインのデータを削除します。
     *
     * @param pluginName         プラグインの名前
     * @param thinDependencyTree 依存関係ツリーを構築するかどうか
     */
    public void removePluginData(@NotNull String pluginName, boolean thinDependencyTree)
    {

        try (Connection connection = this.db.getConnection())
        {
            connection.createStatement().execute("BEGIN TRANSACTION");

            PreparedStatement statement =
                    connection.prepareStatement("DELETE FROM plugin_author WHERE name = ?");
            statement.setString(1, pluginName);
            statement.execute();

            statement = connection.prepareStatement("DELETE FROM depend WHERE name = ?");
            statement.setString(1, pluginName);
            statement.execute();

            statement = connection.prepareStatement("DELETE FROM soft_depend WHERE name = ?");
            statement.setString(1, pluginName);
            statement.execute();

            statement = connection.prepareStatement("DELETE FROM load_before WHERE name = ?");
            statement.setString(1, pluginName);
            statement.execute();

            statement = connection.prepareStatement("DELETE FROM plugin WHERE name = ?");
            statement.setString(1, pluginName);
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

    /**
     * 依存関係ツリーを保存します。
     *
     * @param dependencyNodes 依存関係ツリー
     */
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

    /**
     * 依存関係ツリーを構築します。
     *
     * @param plugin プラグイン
     */
    public void buildDependencyTree(@NotNull Plugin plugin)
    {
        String pluginName = plugin.getName();

        List<String> dependencies = this.getDependOn(pluginName);
        List<String> softDependencies = this.getDependOn(pluginName);
        List<String> loadBefore = this.getLoadBeforeBy(pluginName);

        List<DependencyNode> dependencyNodes = createDependencyNodes(pluginName, dependencies, softDependencies, loadBefore);

        this.saveDependencyTree(dependencyNodes);
    }

    /**
     * 依存関係ツリーを間引きします。
     *
     * @param pluginName プラグインの名前
     */
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

    private void initializeTables()
    {
        try (Connection con = this.db.getConnection())
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

    private List<String> getListFromTable(String tableName, String name, String field)
    {
        return this.getListFromTable(tableName, name, "name", field);
    }

    @SuppressWarnings("SqlResolve")
    private List<String> getListFromTable(String tableName, String name, String queryField, String field)
    {
        List<String> depends = new ArrayList<>();

        try (Connection con = this.db.getConnection())
        {
            PreparedStatement statement =
                    con.prepareStatement("SELECT * FROM " + tableName + " WHERE " + queryField + " = ?");
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
