package org.kunlab.kpm.meta.interfaces;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.db.Transaction;
import org.kunlab.kpm.meta.InstallOperator;
import org.kunlab.kpm.meta.PluginMeta;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * プラグインのメタデータを提供するクラスです。
 */
public interface PluginMetaProvider
{
    /**
     * このクラスを破棄します。
     */
    void close();

    /**
     * プラグインが依存(depend)しているプラグインのリストを取得します。
     *
     * @param pluginName プラグインの名前
     * @return 依存しているプラグインのリスト
     */
    List<DependencyNode> getDependOn(@NotNull String pluginName);

    /**
     * プラグインが依存(soft_depend)しているプラグインのリストを取得します。
     *
     * @param pluginName プラグインの名前
     * @return 依存しているプラグインのリスト
     */
    List<DependencyNode> getSoftDependOn(@NotNull String pluginName);

    /**
     * プラグインを依存(load_before)しているプラグインのリストを取得します。
     * load_before は特殊な依存で, 依存しているプラグインを先に読み込むようにします。
     *
     * @param pluginName プラグインの名前
     * @return 依存しているプラグインのリスト
     */
    List<DependencyNode> getLoadBefore(@NotNull String pluginName);

    /**
     * プラグインが依存(depend)されているプラグインのリストを取得します。
     *
     * @param pluginName プラグインの名前
     * @return 依存されているプラグインのリスト
     */
    List<DependencyNode> getDependedBy(@NotNull String pluginName);

    /**
     * プラグインが依存(soft_depend)されているプラグインのリストを取得します。
     *
     * @param pluginName プラグインの名前
     * @return 依存されているプラグインのリスト
     */
    List<DependencyNode> getSoftDependedBy(@NotNull String pluginName);

    /**
     * プラグインが依存(load_before)されているプラグインのリストを取得します。
     * load_before は特殊な依存で, 依存しているプラグインを先に読み込むようにします。
     *
     * @param pluginName プラグインの名前
     * @return 依存されているプラグインのリスト
     */
    List<DependencyNode> getLoadBeforeBy(@NotNull String pluginName);

    /**
     * プラグインの作者を取得します。
     *
     * @param pluginName プラグインの名前
     * @return プラグインの作者
     */
    List<String> getAuthors(@NotNull String pluginName);

    /**
     * プラグインの作者からプラグインのリストを取得します。
     *
     * @param author プラグインの作者
     * @return プラグインのリスト
     */
    List<String> getPluginsByAuthor(@NotNull String author);

    /**
     * プラグインが誰によってインストールされたかを取得します。
     *
     * @param pluginName プラグインの名前
     * @return プラグインのインストール者
     */
    InstallOperator getInstalledBy(@NotNull String pluginName);

    /**
     * プラグインが依存関係かどうかを取得します。
     * 依存関係としてマークされている場合, 自動削除等の機能の対象になります。
     *
     * @param pluginName プラグインの名前
     * @return 依存関係かどうか
     */
    boolean isDependency(@NotNull String pluginName);

    /**
     * プラグインが依存関係かどうかを設定します。
     *
     * @param pluginName   プラグインの名前
     * @param isDependency 依存関係かどうか
     */
    void setDependencyFlag(@NotNull String pluginName, boolean isDependency);

    /**
     * プラグインの解決クエリを更新します。
     *
     * @param pluginName プラグインの名前
     * @param query      解決クエリ
     */
    void updateResolveQuery(@NotNull String pluginName, @NotNull String query);

    /**
     * プラグインのメタデータが存在しているかどうかを返します。
     *
     * @param pluginName プラグインの名前
     * @return プラグインのメタデータが存在しているかどうか
     */
    boolean isPluginMetaExists(@NotNull String pluginName);

    /**
     * プラグインのメタデータが存在しているかどうかを返します。
     *
     * @param pluginName プラグイン
     * @return プラグインのメタデータが存在しているかどうか
     */
    boolean isPluginMetaExists(@NotNull Plugin pluginName);

    /**
     * プラグインのメタデータを保存します。
     *
     * @param plugin       プラグイン
     * @param installedBy  プラグインのインストール者
     * @param installedAt  プラグインのインストール日時
     * @param resolveQuery プラグインの解決クエリ
     * @param isDependency プラグインが依存関係かどうか
     */
    void savePluginMeta(@NotNull Plugin plugin,
                        @NotNull InstallOperator installedBy,
                        long installedAt,
                        @Nullable String resolveQuery,
                        boolean isDependency);

    void savePluginMeta(@NotNull PluginMeta meta);

    /**
     * プラグインのメタデータを削除します。
     *
     * @param pluginName プラグインの名前
     */
    void removePluginMeta(@NotNull String pluginName);

    /**
     * プラグインのメタデータを削除します。
     *
     * @param pluginName  プラグインの名前
     * @param transaction 使用するトランザクション
     */
    void removePluginMeta(String pluginName, Transaction transaction);

    /**
     * プラグインの依存データを削除します。
     *
     * @param connection 使用するコネクション
     * @param pluginName プラグインの名前
     */
    void removePluginRelationalData(Connection connection, String pluginName) throws SQLException;

    /**
     * プラグインのメタデータを取得します。
     *
     * @param pluginName          プラグインの名前
     * @param includeDependencies 依存関係を含めるかどうか
     * @return プラグインのメタデータ
     */
    @NotNull PluginMeta getPluginMeta(@NotNull String pluginName, boolean includeDependencies, boolean includeAuthors);

    /**
     * プラグインのメタデータを取得します。
     *
     * @param pluginName プラグインの名前
     * @return プラグインのメタデータ
     */
    @NotNull PluginMeta getPluginMeta(@NotNull String pluginName);

    /**
     * 依存関係ツリーを保存します。
     *
     * @param dependencyNodes 依存関係ツリー
     */
    void saveDependencyTree(@NotNull List<DependencyNode> dependencyNodes);

    /**
     * 依存関係ツリーを構築します。
     *
     * @param plugin プラグイン
     */
    void buildDependencyTree(@NotNull Plugin plugin);

    /**
     * 依存関係ツリーを構築します。
     *
     * @param pluginName プラグイン
     */
    void buildDependencyTree(@NotNull String pluginName);

    /**
     * 依存関係ツリーを間引きします。
     *
     * @param pluginName プラグインの名前
     */
    void deleteFromDependencyTree(@NotNull String pluginName);

    /**
     * PluginMeta のイテレータを取得します。
     *
     * @return PluginMeta のイテレータ
     */
    @NotNull PluginMetaIterator getPluginMetaIterator();

    /**
     * 現在管理されているプラグインの数を取得します。
     *
     * @return 現在管理されているプラグインの数
     */
    int countPlugins();

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
    List<String> getUnusedPlugins();
}
