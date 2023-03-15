package org.kunlab.kpm.meta.interfaces;

import org.bukkit.plugin.PluginLoadOrder;
import org.kunlab.kpm.meta.InstallOperator;

import java.util.List;

/**
 * プラグインのメタ情報を表します。
 */
public interface PluginMeta
{
    /**
     * プラグインの名前です。 プラグイン説明ファイル ({@link org.bukkit.plugin.PluginDescriptionFile})の {@code name} と同じです。
     *
     * @return プラグインの名前
     */
    String getName();

    /**
     * プラグインのバージョンです。 プラグイン説明ファイル ({@link org.bukkit.plugin.PluginDescriptionFile})の {@code version} と同じです。
     *
     * @return プラグインのバージョン
     */
    String getVersion();

    /**
     * プラグインのロードタイミングです。 プラグイン説明ファイル ({@link org.bukkit.plugin.PluginDescriptionFile})の {@code load} と同じです。
     *
     * @return プラグインのロードタイミング
     */
    PluginLoadOrder getLoadTiming();

    /**
     * プラグインのインストール者です。
     *
     * @return プラグインのインストール者
     */
    InstallOperator getInstalledBy();

    /**
     * プラグインが依存関係であるかどうかを表します。
     *
     * @return プラグインが依存関係であるかどうか
     */
    boolean isDependency();

    /**
     * プラグインの解決に使用するクエリです。
     * このクエリは, アップグレード機能で使用されます。
     *
     * @return プラグインの解決に使用するクエリ
     */
    String getResolveQuery();

    /**
     * プラグインがいつインストールされたかを表します。
     *
     * @return プラグインがいつインストールされたか
     */
    long getInstalledAt();

    /**
     * プラグインの作者のリストです。 プラグイン説明ファイル ({@link org.bukkit.plugin.PluginDescriptionFile})の {@code authors} と同じです。
     *
     * @return プラグインの作者のリスト
     */
    List<String> getAuthors();

    /**
     * 依存されているプラグインのリストです。
     * この情報は, {@link org.bukkit.plugin.PluginDescriptionFile#getDepend()} および KPM の依存関係ツリーから構築されます。
     *
     * @return 依存されているプラグインのリスト
     */
    List<DependencyNode> getDependedBy();

    /**
     * 依存しているプラグインのリストです。
     * この情報は, {@link org.bukkit.plugin.PluginDescriptionFile#getDepend()} および KPM の依存関係ツリーから構築されます。
     *
     * @return 依存しているプラグインのリスト
     */
    List<DependencyNode> getDependsOn();
}
