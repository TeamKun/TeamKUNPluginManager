package net.kunmc.lab.teamkunpluginmanager.plugin.meta;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoadOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * プラグインのメタデータを提供するクラスです。
 */
@SuppressWarnings("unused")
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
    public List<DependencyNode> getDependOn(@NotNull String pluginName)
    {
        return getDependDataFromTable("depend", pluginName, "dependency",
                DependType.HARD_DEPEND, false
        );
    }

    /**
     * プラグインが依存(soft_depend)しているプラグインのリストを取得します。
     *
     * @param pluginName プラグインの名前
     * @return 依存しているプラグインのリスト
     */
    public List<DependencyNode> getSoftDependOn(@NotNull String pluginName)
    {
        return getDependDataFromTable("soft_depend", pluginName, "soft_dependency",
                DependType.SOFT_DEPEND, false
        );
    }

    /**
     * プラグインを依存(load_before)しているプラグインのリストを取得します。
     * load_before は特殊な依存で, 依存しているプラグインを先に読み込むようにします。
     *
     * @param pluginName プラグインの名前
     * @return 依存しているプラグインのリスト
     */
    public List<DependencyNode> getLoadBefore(@NotNull String pluginName)
    {
        return getDependDataFromTable("load_before", pluginName, "load_before",
                DependType.LOAD_BEFORE, false
        );
    }

    /**
     * プラグインが依存(depend)されているプラグインのリストを取得します。
     *
     * @param pluginName プラグインの名前
     * @return 依存されているプラグインのリスト
     */
    public List<DependencyNode> getDependedBy(@NotNull String pluginName)
    {
        return getDependDataFromTable("depend", pluginName, "dependency",
                DependType.HARD_DEPEND, true
        );
    }

    /**
     * プラグインが依存(soft_depend)されているプラグインのリストを取得します。
     *
     * @param pluginName プラグインの名前
     * @return 依存されているプラグインのリスト
     */
    public List<DependencyNode> getSoftDependedBy(@NotNull String pluginName)
    {
        return getDependDataFromTable("soft_depend", pluginName, "soft_dependency",
                DependType.SOFT_DEPEND, true
        );
    }

    /**
     * プラグインが依存(load_before)されているプラグインのリストを取得します。
     * load_before は特殊な依存で, 依存しているプラグインを先に読み込むようにします。
     *
     * @param pluginName プラグインの名前
     * @return 依存されているプラグインのリスト
     */
    public List<DependencyNode> getLoadBeforeBy(@NotNull String pluginName)
    {
        return getDependDataFromTable("load_before", pluginName, "load_before",
                DependType.LOAD_BEFORE, true
        );
    }

    /**
     * プラグインの作者を取得します。
     *
     * @param pluginName プラグインの名前
     * @return プラグインの作者
     */
    public List<String> getAuthors(@NotNull String pluginName)
    {
        return getListFromTable("plugin_author", pluginName, "name", "author");
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
            PreparedStatement statement = con.prepareStatement("SELECT * FROM plugin_meta WHERE name = ?");
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
            PreparedStatement statement = con.prepareStatement("SELECT * FROM plugin_meta WHERE name = ?");
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
            PreparedStatement statement = con.prepareStatement("UPDATE plugin_meta SET is_dependency = ? WHERE name = ?");
            statement.setInt(1, isDependency ? 0: 1);
            statement.setString(2, pluginName);

            statement.executeUpdate();

            con.createStatement().execute("COMMIT TRANSACTION");
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * プラグインの解決クエリを更新します。
     *
     * @param pluginName プラグインの名前
     * @param query      解決クエリ
     */
    public void updateResolveQuery(@NotNull String pluginName, @NotNull String query)
    {
        Connection con = null;
        try
        {
            con = this.db.getConnection();
            con.createStatement().execute("BEGIN TRANSACTION");
            // Check if the plugin exists
            PreparedStatement statement = con.prepareStatement("SELECT * FROM plugin_meta WHERE name = ?");
            statement.setString(1, pluginName);

            if (!statement.executeQuery().next())
            {
                con.createStatement().execute("ROLLBACK TRANSACTION");
                throw new IllegalArgumentException("Plugin does not exist");
            }

            statement = con.prepareStatement("UPDATE plugin_meta SET resolve_query = ? WHERE name = ?");
            statement.setString(1, query);
            statement.setString(2, pluginName);

            statement.executeUpdate();

            con.createStatement().execute("COMMIT TRANSACTION");
        }
        catch (SQLException e)
        {
            if (con != null)
            {
                try
                {
                    con.rollback();
                }
                catch (SQLException ex)
                {
                    System.err.println("Failed to rollback transaction");
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException(e);
        }
        finally
        {
            if (con != null)
            {
                try
                {
                    con.close();
                }
                catch (SQLException e)
                {
                    System.err.println("Failed to close connection");
                    e.printStackTrace();
                }
            }
        }
    }

    private void savePluginRelationalData(Connection connection, Plugin plugin) throws SQLException
    {
        PluginDescriptionFile description = plugin.getDescription();

        String name = description.getName();
        String version = description.getVersion();
        List<String> authors = description.getAuthors();
        String loadTiming = description.getLoad().name();

        List<String> dependencies = description.getDepend();
        List<String> softDependencies = description.getSoftDepend();
        List<String> loadBefore = description.getLoadBefore();

        PreparedStatement statement =
                connection.prepareStatement("INSERT INTO plugin_author(name, author) VALUES(?, ?)");
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

    }

    /**
     * プラグインのメタデータが存在しているかどうかを返します。
     *
     * @param pluginName プラグインの名前
     * @return プラグインのメタデータが存在しているかどうか
     */
    public boolean isPluginMetaExists(@NotNull String pluginName)
    {
        try (Connection con = this.db.getConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM plugin_meta WHERE name = ?");
            statement.setString(1, pluginName);

            return statement.executeQuery().next();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * プラグインのメタデータを保存します。
     *
     * @param plugin       プラグイン
     * @param installedBy  プラグインのインストール者
     * @param installedAt  プラグインのインストール日時
     * @param resolveQuery プラグインの解決クエリ
     * @param isDependency プラグインが依存関係かどうか
     */
    public void savePluginMeta(@NotNull Plugin plugin,
                               @NotNull InstallOperator installedBy,
                               long installedAt,
                               @Nullable String resolveQuery,
                               boolean isDependency)

    {

        Connection con = null;
        try
        {
            con = this.db.getConnection();

            PreparedStatement statement =
                    con.prepareStatement("INSERT INTO plugin_meta(name, version, load_timing, installed_at, installed_by, resolve_query, is_dependency) VALUES(?, ?, ?, ?, ?, ?, ?)");

            PluginDescriptionFile description = plugin.getDescription();

            statement.setString(1, description.getName());
            statement.setString(2, description.getVersion());
            statement.setString(3, description.getLoad().name());
            statement.setLong(4, installedAt);
            statement.setString(5, installedBy.name());
            statement.setString(6, resolveQuery);
            statement.setInt(7, isDependency ? 1: 0);

            statement.executeUpdate();

            this.savePluginRelationalData(con, plugin);

            con.commit();
        }
        catch (SQLException e)
        {
            if (con != null)
            {
                try
                {
                    con.rollback();
                }
                catch (SQLException ex)
                {
                    System.err.println("Failed to rollback transaction");
                    ex.printStackTrace();
                }
            }

            throw new RuntimeException(e);
        }
        finally
        {
            if (con != null)
            {
                try
                {
                    con.close();
                }
                catch (SQLException e)
                {
                    System.err.println("Failed to close connection");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * プラグインのメタデータを削除します。
     *
     * @param pluginName プラグインの名前
     */
    public void removePluginMeta(@NotNull String pluginName)
    {
        Connection con = null;
        try
        {
            con = this.db.getConnection();

            this.removePluginRelationalData(con, pluginName);

            PreparedStatement statement =
                    con.prepareStatement("DELETE FROM plugin_meta WHERE name = ?");
            statement.setString(1, pluginName);

            statement.executeUpdate();


            con.commit();
        }
        catch (SQLException e)
        {
            if (con != null)
            {
                try
                {
                    con.rollback();
                }
                catch (SQLException ex)
                {
                    System.err.println("Failed to rollback transaction");
                    ex.printStackTrace();
                }
            }

            throw new RuntimeException(e);
        }
        finally
        {
            if (con != null)
            {
                try
                {
                    con.close();
                }
                catch (SQLException e)
                {
                    System.err.println("Failed to close connection");
                    e.printStackTrace();
                }
            }
        }
    }

    private void removePluginRelationalData(Connection connection, String pluginName) throws SQLException
    {
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

        statement = connection.prepareStatement("DELETE FROM plugin_meta WHERE name = ?");
        statement.setString(1, pluginName);
        statement.execute();
    }

    /**
     * プラグインのメタデータを取得します。
     *
     * @param pluginName          プラグインの名前
     * @param includeDependencies 依存関係を含めるかどうか
     * @return プラグインのメタデータ
     */
    public @Nullable PluginMeta getPluginMeta(@NotNull String pluginName, boolean includeDependencies)
    {
        Connection con;
        try
        {
            con = this.db.getConnection();

            PreparedStatement statement =
                    con.prepareStatement("SELECT * FROM plugin_meta WHERE name = ?");
            statement.setString(1, pluginName);

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next())
                return null;

            String name = resultSet.getString("name");
            String version = resultSet.getString("version");
            PluginLoadOrder loadTiming = PluginLoadOrder.valueOf(resultSet.getString("load_timing"));
            long installedAt = resultSet.getLong("installed_at");
            InstallOperator installedBy = InstallOperator.valueOf(resultSet.getString("installed_by"));
            String resolveQuery = resultSet.getString("resolve_query");
            boolean isDependency = resultSet.getInt("is_dependency") == 1;

            List<DependencyNode> dependedBy = new ArrayList<>();
            List<DependencyNode> dependsOn = new ArrayList<>();
            if (includeDependencies)
            {
                dependedBy = this.getDependedBy(pluginName);
                dependsOn = this.getDependOn(pluginName);
            }

            return new PluginMeta(
                    name,
                    version,
                    loadTiming,
                    installedBy,
                    isDependency,
                    resolveQuery,
                    installedAt,
                    dependedBy,
                    dependsOn
            );
        }
        catch (SQLException e)
        {
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
        Connection con = null;

        try
        {
            con = this.db.getConnection();

            PreparedStatement statement =
                    con.prepareStatement("INSERT INTO dependency_tree(name, parent, depend_type) VALUES(?, ?, ?)");

            for (DependencyNode node : dependencyNodes)
            {
                statement.setString(1, node.getPlugin());
                statement.setString(2, node.getDependsOn());
                statement.setString(3, node.getDependType().name());
                statement.execute();
            }

            con.commit();
        }
        catch (SQLException e)
        {
            if (con != null)
            {
                try
                {
                    con.rollback();
                }
                catch (SQLException ex)
                {
                    System.err.println("Failed to rollback transaction");
                    ex.printStackTrace();
                }
            }

            throw new RuntimeException(e);
        }
        finally
        {
            if (con != null)
            {
                try
                {
                    con.close();
                }
                catch (SQLException e)
                {
                    System.err.println("Failed to close connection");
                    e.printStackTrace();
                }
            }
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

        List<DependencyNode> dependencies = this.getDependOn(pluginName);
        List<DependencyNode> softDependencies = this.getSoftDependOn(pluginName);
        List<DependencyNode> loadBefore = this.getLoadBeforeBy(pluginName);

        dependencies.addAll(softDependencies);
        dependencies.addAll(loadBefore);

        this.saveDependencyTree(dependencies);
    }

    /**
     * 依存関係ツリーを間引きします。
     *
     * @param pluginName プラグインの名前
     */
    public void deleteFromDependencyTree(@NotNull String pluginName)
    {
        Connection con = null;
        try
        {
            con = this.db.getConnection();

            PreparedStatement statement =
                    con.prepareStatement("DELETE FROM dependency_tree WHERE name = ?");
            statement.setString(1, pluginName);
            statement.execute();

            con.commit();
        }
        catch (SQLException e)
        {
            if (con != null)
            {
                try
                {
                    con.rollback();
                }
                catch (SQLException ex)
                {
                    System.err.println("Failed to rollback transaction");
                    ex.printStackTrace();
                }
            }

            throw new RuntimeException(e);
        }
        finally
        {
            if (con != null)
            {
                try
                {
                    con.close();
                }
                catch (SQLException e)
                {
                    System.err.println("Failed to close connection");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 使用されていないプラグインのリストを取得します。
     *
     * @return 使用されていないプラグインのリスト
     */
    public List<String> getUnusedPlugins()
    {
        Connection con = null;
        try
        {
            con = this.db.getConnection();

            PreparedStatement statement =
                    con.prepareStatement(
                            "WITH RECURSIVE cte AS (" +
                                    "SELECT name FROM plugin_meta WHERE name NOT IN (SELECT parent FROM dependency_tree) AND is_dependency = 1 " +
                                    "UNION ALL " +
                                    "SELECT dependency_tree.name FROM dependency_tree INNER JOIN cte ON dependency_tree.parent = cte.name" +
                                    ")" +
                                    "SELECT name FROM cte"
                    );

            ResultSet resultSet = statement.executeQuery();
            List<String> unusedPlugins = new ArrayList<>();

            while (resultSet.next())
                unusedPlugins.add(resultSet.getString("name"));

            return unusedPlugins;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if (con != null)
            {
                try
                {
                    con.close();
                }
                catch (SQLException e)
                {
                    System.err.println("Failed to close connection");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * すべてのプラグインのデータをクロールします。
     */
    public void crawlAll()
    {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

        for (Plugin plugin : plugins)
        {
            if (this.isPluginMetaExists(plugin.getName()))
                continue;

            this.savePluginMeta(
                    plugin,
                    InstallOperator.SERVER_ADMIN,
                    System.currentTimeMillis(),
                    null,
                    false
            );

            this.buildDependencyTree(plugin);
        }
    }

    private void initializeTables()
    {
        try (Connection con = this.db.getConnection())
        {
            Statement statement = con.createStatement();

            statement.execute("CREATE TABLE IF NOT EXISTS plugin_meta(" +
                    "name TEXT NOT NULL PRIMARY KEY," +
                    "version TEXT NOT NULL," +
                    "load_timing TEXT NOT NULL," +
                    "installed_at INTEGER NOT NULL," +
                    "installed_by TEXT NOT NULL," +
                    "resolve_query TEXT," +
                    "is_dependency INTEGER(1) NOT NULL" +
                    ")"
            );

            statement.execute("CREATE TABLE IF NOT EXISTS plugin_author(" +
                    "name TEXT NOT NULL UNIQUE," +
                    "author TEXT NOT NULL" +
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
                    "name TEXT NOT NULL," +
                    "parent TEXT," +
                    "depend_type TEXT," +
                    "PRIMARY KEY (name, parent)" +
                    ")"
            );

            con.commit();
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

    private List<DependencyNode> getDependDataFromTable(String tableName, String name, String field, DependType type, boolean baseReversed)
    {
        List<String> depString = this.getListFromTable(tableName, name, baseReversed ? field: "name", baseReversed ? "name": field);

        if (baseReversed)
            return depString.stream()
                    .map(item -> new DependencyNode(item, name, type))
                    .collect(Collectors.toList());
        else
            return depString.stream()
                    .map(item -> new DependencyNode(name, item, type))
                    .collect(Collectors.toList());

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
