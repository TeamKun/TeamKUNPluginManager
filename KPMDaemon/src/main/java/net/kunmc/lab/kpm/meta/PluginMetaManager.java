package net.kunmc.lab.kpm.meta;

import lombok.Getter;
import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.KPMEnvironment;
import net.kunmc.lab.kpm.utils.PluginUtil;
import net.kunmc.lab.kpm.utils.ServerConditionChecker;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * プラグインのメタデータを管理するクラスです。
 */
public class PluginMetaManager implements Listener
{
    private final KPMDaemon daemon;
    /**
     * メタデータを提供するプロバイダです。
     */
    @Getter
    private final PluginMetaProvider provider;
    private final List<String> exceptedPluginModifications;

    public PluginMetaManager(@NotNull KPMDaemon daemon, @NotNull KPMEnvironment env)
    {
        this.daemon = daemon;
        this.provider = new PluginMetaProvider(env.getPlugin(), env.getMetadataDBPath());
        this.exceptedPluginModifications = new ArrayList<>();

        // Below lambda will be executed after all plugins are loaded.
        // (Bukkit runs task after all plugins are loaded.)
        Runner.runLater(() -> Bukkit.getPluginManager().registerEvents(this, env.getPlugin()), 1L);
    }

    /**
     * プラグインの変更(変更や追加)を準備します。
     * このメソッドを呼び出した後にプラグインを変更すると, 自動的なメタデータの更新が行われません。
     *
     * @param pluginName プラグインの名前
     */
    public void preparePluginModify(@NotNull String pluginName)
    {
        synchronized (this.exceptedPluginModifications)
        {
            if (!this.exceptedPluginModifications.contains(pluginName))
                this.exceptedPluginModifications.add(pluginName);
        }
    }

    /**
     * プラグインの変更(変更や追加)を準備します。
     * このメソッドを呼び出した後にプラグインを変更すると, 自動的なメタデータの更新が行われません。
     *
     * @param plugin プラグイン
     * @see #preparePluginModify(String)
     */
    public void preparePluginModify(@NotNull Plugin plugin)
    {
        this.preparePluginModify(plugin.getName());
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event)
    {
        Plugin plugin = event.getPlugin();
        if (plugin.getName().equalsIgnoreCase("TeamKUNPluginManager") ||
                this.checkNoAutoCreateMetadata(plugin))
            return;

        String pluginNameFull = PluginUtil.getPluginString(plugin);

        this.daemon.getLogger().info("プラグインの追加が検出されました: " + pluginNameFull);

        this.daemon.getLogger().info("プラグインのメタデータを作成しています ...");
        this.onInstalled(plugin, InstallOperator.UNKNOWN, null, false);

        this.daemon.getLogger().info("依存関係ツリーを構築しています ...");
        this.provider.buildDependencyTree(plugin);

    }

    @EventHandler
    public void onDisable(PluginDisableEvent event)
    {
        ServerConditionChecker conditions = this.daemon.getServerConditionChecker();
        if (conditions.isStopping() || conditions.isReloading())
            return;  // The server is being stopped.

        Plugin plugin = event.getPlugin();
        if (plugin.getName().equalsIgnoreCase("TeamKUNPluginManager") ||
                this.checkNoAutoCreateMetadata(plugin))
            return;

        String pluginNameFull = PluginUtil.getPluginString(plugin);

        this.daemon.getLogger().info("プラグインの削除が検出されました: " + pluginNameFull);

        this.daemon.getLogger().info("プラグインのメタデータを削除しています ...");
        this.onUninstalled(pluginNameFull);

        this.daemon.getLogger().info("依存関係ツリーを構築しています ...");
        this.provider.deleteFromDependencyTree(pluginNameFull);
    }

    /**
     * プラグインがインストールされたときに呼び出します。
     *
     * @param plugin       インストールされたプラグイン
     * @param operator     インストールした操作者
     * @param resolveQuery インストール時に使用したプラグイン解決クエリ
     * @param installedAt  インストールされた時刻
     */
    public void onInstalled(@NotNull Plugin plugin, @NotNull InstallOperator operator, @Nullable String resolveQuery, long installedAt, boolean isDependency)
    {
        this.provider.savePluginMeta(
                plugin,
                operator,
                installedAt,
                resolveQuery,
                isDependency
        );
    }

    /**
     * プラグインがインストールされたときに呼び出します。
     *
     * @param plugin       インストールされたプラグイン
     * @param operator     インストールした操作者
     * @param resolveQuery インストール時に使用したプラグイン解決クエリ
     */
    public void onInstalled(@NotNull Plugin plugin, @NotNull InstallOperator operator, @Nullable String resolveQuery, boolean isDependency)
    {
        this.onInstalled(plugin, operator, resolveQuery, System.currentTimeMillis(), isDependency);
    }

    /**
     * プラグインがアンインストールされたときに呼び出します。
     *
     * @param pluginName アンインストールされたプラグインの名前
     */
    public void onUninstalled(@NotNull String pluginName)
    {
        this.provider.removePluginMeta(pluginName);
    }

    /**
     * プラグインのメタデータが存在するかどうかを返します。
     *
     * @param pluginName プラグインの名前
     * @return プラグインのメタデータが存在するかどうか
     */
    public boolean hasPluginMeta(@NotNull String pluginName)
    {
        return this.provider.isPluginMetaExists(pluginName);
    }

    /**
     * プラグインのメタデータが存在するかどうかを返します。
     *
     * @param plugin プラグイン
     * @return プラグインのメタデータが存在するかどうか
     */
    public boolean hasPluginMeta(@NotNull Plugin plugin)
    {
        return this.hasPluginMeta(plugin.getName());
    }

    private boolean checkNoAutoCreateMetadata(Plugin plugin)
    {
        synchronized (this.exceptedPluginModifications)
        {
            return this.exceptedPluginModifications.remove(plugin.getName());
        }
    }
}
