package net.kunmc.lab.teamkunpluginmanager.meta;

import com.zaxxer.hikari.HikariDataSource;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.utils.db.Transaction;
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
        this.db = Transaction.createDataSource(databasePath);

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
        this.db.close();
    }

    /**
     * プラグインが依存(depend)しているプラグインのリストを取得します。
     *
     * @param pluginName プラグインの名前
     * @return 依存しているプラグインのリスト
     */
    public List<DependencyNode> getDependOn(@NotNull String pluginName)
    {
        return this.getDependDataFromTable("depend", pluginName, "dependency",
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
        return this.getDependDataFromTable("soft_depend", pluginName, "soft_dependency",
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
        return this.getDependDataFromTable("load_before", pluginName, "load_before",
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
        return this.getDependDataFromTable("depend", pluginName, "dependency",
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
        return this.getDependDataFromTable("soft_depend", pluginName, "soft_dependency",
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
        return this.getDependDataFromTable("load_before", pluginName, "load_before",
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
        return MetaSQLUtil.getListFromTable(this.db, "plugin_author", pluginName, "name", "author");
    }

    /**
     * プラグインの作者からプラグインのリストを取得します。
     *
     * @param author プラグインの作者
     * @return プラグインのリスト
     */
    public List<String> getPluginsByAuthor(@NotNull String author)
    {
        return MetaSQLUtil.getListFromTable(this.db, "plugin_author", author, "author", "name");
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
            e.printStackTrace();
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
        Transaction.create(this.db, "UPDATE plugin_meta SET is_dependency = ? WHERE name = ?")
                .set(1, isDependency ? 1: 0)
                .set(2, pluginName)
                .executeUpdate();

    }

    /**
     * プラグインの解決クエリを更新します。
     *
     * @param pluginName プラグインの名前
     * @param query      解決クエリ
     */
    public void updateResolveQuery(@NotNull String pluginName, @NotNull String query)
    {
        Transaction transaction = Transaction.create(this.db, "SELECT * FROM plugin_meta WHERE name = ?")
                .set(1, pluginName);

        if (!transaction.isExists())
            return;

        transaction.renew("UPDATE plugin_meta SET resolve_query = ? WHERE name = ?")
                .set(1, query)
                .set(2, pluginName)
                .executeUpdate();
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

    private void savePluginRelationalData(Connection connection, PluginMeta meta) throws SQLException
    {
        String name = meta.getName();
        String version = meta.getVersion();
        String loadTiming = meta.getLoadTiming().name();
        List<String> authors = meta.getAuthors();
        List<DependencyNode> dependsOn = meta.getDependsOn();
        List<String> dependencies = MetaSQLUtil.getStringDependsOn(dependsOn, DependType.HARD_DEPEND);
        List<String> softDependencies = MetaSQLUtil.getStringDependsOn(dependsOn, DependType.SOFT_DEPEND);
        List<String> loadBefore = MetaSQLUtil.getStringDependsOn(dependsOn, DependType.LOAD_BEFORE);

        Transaction transaction =
                Transaction.create(connection, "INSERT INTO plugin_author(name, author) VALUES(?, ?)")
                        .set(1, name);

        for (String author : authors)
        {
            transaction.set(2, author);
            transaction.executeUpdate(false);
        }

        MetaSQLUtil.deleteAndSaveDepends(connection, "depend", "dependency", name, dependencies);
        MetaSQLUtil.deleteAndSaveDepends(connection, "soft_depend", "soft_dependency", name, softDependencies);
        MetaSQLUtil.deleteAndSaveDepends(connection, "load_before", "load_before", name, loadBefore);

        Transaction.create(connection, "UPDATE plugin_meta SET version = ?, load_timing = ? WHERE name = ?")
                .set(1, version)
                .set(2, loadTiming)
                .set(3, name)
                .executeUpdate(false);
    }

    /**
     * プラグインのメタデータが存在しているかどうかを返します。
     *
     * @param pluginName プラグインの名前
     * @return プラグインのメタデータが存在しているかどうか
     */
    public boolean isPluginMetaExists(@NotNull String pluginName)
    {
        return Transaction.create(this.db, "SELECT * FROM plugin_meta WHERE name = ?")
                .set(1, pluginName)
                .isExists();
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

        PluginDescriptionFile description = plugin.getDescription();
        Transaction.create(
                        this.db,
                        "INSERT INTO plugin_meta(name, version, load_timing, installed_at, installed_by, resolve_query, is_dependency) VALUES(?, ?, ?, ?, ?, ?, ?)"
                )
                .set(1, description.getName())
                .set(2, description.getVersion())
                .set(3, description.getLoad().name())
                .set(4, installedAt)
                .set(5, installedBy.name())
                .set(6, resolveQuery)
                .set(7, isDependency ? 1: 0)
                .beforeCommit(tr -> this.savePluginRelationalData(tr.getConnection(), plugin))
                .executeUpdate();
    }

    public void savePluginMeta(@NotNull PluginMeta meta)
    {
        Transaction.create(
                        this.db,
                        "INSERT OR REPLACE INTO plugin_meta(name, version, load_timing, installed_at, installed_by, resolve_query, is_dependency) VALUES(?, ?, ?, ?, ?, ?, ?)"
                )
                .set(1, meta.getName())
                .set(2, meta.getVersion())
                .set(3, meta.getLoadTiming().name())
                .set(4, meta.getInstalledAt())
                .set(5, meta.getInstalledBy().name())
                .set(6, meta.getResolveQuery())
                .set(7, meta.isDependency() ? 1: 0)
                .beforeCommit(tr -> this.savePluginRelationalData(tr.getConnection(), meta))
                .executeUpdate();
    }

    /**
     * プラグインのメタデータを削除します。
     *
     * @param pluginName プラグインの名前
     */
    public void removePluginMeta(@NotNull String pluginName)
    {
        Transaction.create(this.db, "DELETE FROM plugin_meta WHERE name = ?")
                .set(1, pluginName)
                .beforeCommit(tr -> this.removePluginRelationalData(tr.getConnection(), pluginName))
                .executeUpdate();
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
    public @NotNull PluginMeta getPluginMeta(@NotNull String pluginName, boolean includeDependencies, boolean includeAuthors)
    {
        try (Connection con = this.db.getConnection())
        {
            PreparedStatement statement =
                    con.prepareStatement("SELECT * FROM plugin_meta WHERE name = ?");
            statement.setString(1, pluginName);

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next())
                throw new IllegalArgumentException("Plugin " + pluginName + " is not exists");

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

                dependedBy.addAll(this.getSoftDependedBy(pluginName));
                dependsOn.addAll(this.getSoftDependOn(pluginName));

                dependedBy.addAll(this.getLoadBeforeBy(pluginName));
                dependsOn.addAll(this.getLoadBefore(pluginName));
            }

            List<String> authors = new ArrayList<>();
            if (includeAuthors)
                authors = this.getAuthors(pluginName);

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
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * プラグインのメタデータを取得します。
     *
     * @param pluginName プラグインの名前
     * @return プラグインのメタデータ
     */
    public @NotNull PluginMeta getPluginMeta(@NotNull String pluginName)
    {
        return this.getPluginMeta(pluginName, true, true);
    }

    /**
     * 依存関係ツリーを保存します。
     *
     * @param dependencyNodes 依存関係ツリー
     */
    public void saveDependencyTree(@NotNull List<DependencyNode> dependencyNodes)
    {
        Transaction transaction = Transaction.create(
                this.db,
                "INSERT INTO dependency_tree(name, parent, depend_type) VALUES(?, ?, ?)"
        );

        for (DependencyNode node : dependencyNodes)
        {
            transaction
                    .set(1, node.getPlugin())
                    .set(2, node.getDependsOn())
                    .set(3, node.getDependType().name())
                    .executeUpdate(false);
        }

        transaction.finishManually();
    }

    /**
     * 依存関係ツリーを構築します。
     *
     * @param plugin プラグイン
     */
    public void buildDependencyTree(@NotNull Plugin plugin)
    {
        this.buildDependencyTree(plugin.getName());
    }

    /**
     * 依存関係ツリーを構築します。
     *
     * @param pluginName プラグイン
     */
    public void buildDependencyTree(@NotNull String pluginName)
    {
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
        Transaction.create(this.db, "DELETE FROM dependency_tree WHERE name = ?")
                .set(1, pluginName)
                .executeUpdate();
    }

    /**
     * 使われていない依存関係のリストを取得します。
     * <p>
     * (プラグインが依存関係であるかどうかは、plugin_meta.is_dependencyを使用して確認できます)
     * 関係はdependency_treeテーブルに格納されています。
     * </p>
     *
     * <p>
     * 例：サーバにインストールされているプラグイン：
     * PluginAは依存関係
     * PluginBは依存関係
     * PluginCは依存関係ではない
     * </p>
     *
     * <p>
     * 例1：
     * <ul>
     *     <li>PluginAはPluginBに依存している</li>
     *     <li>PluginBとPluginCの間には依存関係はない</li>
     * </ul>
     * この場合、このメソッドはPluginAとPluginBを返します。
     * なぜなら、PluginBはPluginAによって使用されていますが、PluginAはどのプラグインにも使用されていないからです。
     * </p>
     *
     * <p>
     * 例2：
     * <ul>
     *     <li>PluginCはPluginAに依存している</li>
     *     <li>PluginAはPluginBに依存している</li>
     * </ul>
     * この場合、このメソッドはどのプラグインも返しません。
     * なぜなら、PluginAはPluginCによって使用されていますが、PluginCは依存関係ではなく、サーバがこのプラグインを使用しているためです。
     * </p>
     *
     * @return 使用されていないプラグインのリスト
     */
    public List<String> getUnusedPlugins()
    {
        List<String> unusedPlugins = new ArrayList<>();

        try (Connection con = this.db.getConnection())
        {
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT name FROM plugin_meta WHERE is_dependency = 1");

            while (resultSet.next())
            {
                String name = resultSet.getString("name");
                int checkUnused = this.isUnusedRecursive(con, name, 0);
                if (checkUnused == 0) // 0 = unused
                    unusedPlugins.add(name);
            }
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }

        return unusedPlugins;
    }

    // -1: Error
    // 0: Unused
    // 1: Used
    private int isUnusedRecursive(Connection con, @NotNull String pluginName, int depth) throws SQLException
    {
        if (depth > 10)
            return -1;

        PreparedStatement statement = con.prepareStatement("SELECT name, parent FROM dependency_tree WHERE parent = ?");
        PreparedStatement checkIsDependency =
                con.prepareStatement("SELECT COUNT(name) FROM plugin_meta WHERE name = ? AND is_dependency = 1");
        statement.setString(1, pluginName);
        ResultSet resultSet = statement.executeQuery();

        if (this.isNotDependencyInternal(con, checkIsDependency, pluginName))
            return 0;

        while (resultSet.next())
        {
            String name = resultSet.getString("name");
            String parent = resultSet.getString("parent");

            if (this.isNotDependencyInternal(con, checkIsDependency, name))
                return 1;

            int checkUnused = this.isUnusedRecursive(con, name, depth + 1);
            if (checkUnused == 1)
                return 1;
        }

        return 0;
    }

    private boolean isNotDependencyInternal(Connection con, PreparedStatement stmt, String target) throws SQLException
    {
        stmt.setString(1, target);
        ResultSet resultSet = stmt.executeQuery();
        if (resultSet.next())
        {
            int count = resultSet.getInt(1);
            resultSet.close();
            return count == 0;
        }

        resultSet.close();
        return false;
    }

    private void initializeTables()
    {
        Transaction.create(this.db)
                .doTransaction((tr) -> {
                    Statement statement = tr.getConnection().createStatement();

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
                });
    }

    private List<DependencyNode> getDependDataFromTable(String tableName, String name, String field, DependType type, boolean baseReversed)
    {
        List<String> depString = MetaSQLUtil.getListFromTable(this.db, tableName, name, baseReversed ? field: "name", baseReversed ? "name": field);

        if (baseReversed)
            return depString.stream()
                    .map(item -> new DependencyNode(item, name, type))
                    .collect(Collectors.toList());
        else
            return depString.stream()
                    .map(item -> new DependencyNode(name, item, type))
                    .collect(Collectors.toList());

    }
}
