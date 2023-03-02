package org.kunlab.kpm.meta.interfaces;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.meta.InstallOperator;

/**
 * プラグインのメタデータを管理するクラスです。
 */
public interface PluginMetaManager extends Listener
{
    /**
     * プラグインの変更(変更や追加)を準備します。
     * このメソッドを呼び出した後にプラグインを変更すると, 自動的なメタデータの更新が行われません。
     *
     * @param pluginName プラグインの名前
     */
    void preparePluginModify(@NotNull String pluginName);

    /**
     * プラグインの変更(変更や追加)を準備します。
     * このメソッドを呼び出した後にプラグインを変更すると, 自動的なメタデータの更新が行われません。
     *
     * @param plugin プラグイン
     * @see #preparePluginModify(String)
     */
    void preparePluginModify(@NotNull Plugin plugin);

    /**
     * プラグインがインストールされたときに呼び出します。
     *
     * @param plugin       インストールされたプラグイン
     * @param operator     インストールした操作者
     * @param resolveQuery インストール時に使用したプラグイン解決クエリ
     * @param installedAt  インストールされた時刻
     */
    void onInstalled(@NotNull Plugin plugin, @NotNull InstallOperator operator, @Nullable String resolveQuery, long installedAt, boolean isDependency);

    /**
     * プラグインがインストールされたときに呼び出します。
     *
     * @param plugin       インストールされたプラグイン
     * @param operator     インストールした操作者
     * @param resolveQuery インストール時に使用したプラグイン解決クエリ
     */
    void onInstalled(@NotNull Plugin plugin, @NotNull InstallOperator operator, @Nullable String resolveQuery, boolean isDependency);

    /**
     * プラグインがアンインストールされたときに呼び出します。
     *
     * @param pluginName アンインストールされたプラグインの名前
     */
    void onUninstalled(@NotNull String pluginName);

    /**
     * プラグインのメタデータが存在するかどうかを返します。
     *
     * @param pluginName プラグインの名前
     * @return プラグインのメタデータが存在するかどうか
     */
    boolean hasPluginMeta(@NotNull String pluginName);

    /**
     * プラグインのメタデータが存在するかどうかを返します。
     *
     * @param plugin プラグイン
     * @return プラグインのメタデータが存在するかどうか
     */
    boolean hasPluginMeta(@NotNull Plugin plugin);

    /**
     * サーバにインストールされているプラグインのメタデータを構築し、存在しないプラグインのメタデータを削除します。
     */
    void crawlAll();

    PluginMetaProvider getProvider();
}
