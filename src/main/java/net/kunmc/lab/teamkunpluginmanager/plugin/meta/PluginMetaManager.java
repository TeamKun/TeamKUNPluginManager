package net.kunmc.lab.teamkunpluginmanager.plugin.meta;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * プラグインのメタデータを管理するクラスです。
 */
public class PluginMetaManager implements Listener
{
    /**
     * メタデータを提供するプロバイダです。
     */
    @Getter
    private final PluginMetaProvider provider;
    private final List<String> exceptedPluginModifications;

    public PluginMetaManager(@NotNull Plugin plugin, @NotNull Path databasePath)
    {
        this.provider = new PluginMetaProvider(plugin, databasePath);
        this.exceptedPluginModifications = new ArrayList<>();

        // Below lambda will be executed after all plugins are loaded.
        // (Bukkit runs task after all plugins are loaded.)
        Runner.runLater(() -> Bukkit.getPluginManager().registerEvents(this, plugin), 1L);
    }

    /**
     * プラグインの変更(変更や追加)を準備します。
     * このメソッドを呼び出した後にプラグインを変更すると, 自動的なメタデータの更新が行われません。
     *
     * @param pluginName プラグインの名前
     */
    public void preparePluginModify(@NotNull String pluginName)
    {
        this.exceptedPluginModifications.add(pluginName);
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
        if (plugin.getName().equals(TeamKunPluginManager.getPlugin().getName()) ||
                this.checkNoAutoCreateMetadata(plugin))
            return;

        String pluginNameFull = PluginUtil.getPluginString(plugin);

        System.out.println("プラグインの追加が検出されました: " + pluginNameFull);

        System.out.println("プラグインのメタデータを作成してします ...");
        this.onInstalled(plugin, InstallOperator.SERVER_ADMIN, null, false);

        System.out.println("依存関係ツリーを構築しています ...");
        this.provider.buildDependencyTree(plugin);

    }

    @EventHandler
    public void onDisable(PluginDisableEvent event)
    {
        Plugin plugin = event.getPlugin();
        if (plugin.getName().equals(TeamKunPluginManager.getPlugin().getName()) ||
                this.checkNoAutoCreateMetadata(plugin))
            return;

        String pluginNameFull = PluginUtil.getPluginString(plugin);

        System.out.println("プラグインの削除が検出されました: " + pluginNameFull);

        System.out.println("プラグインのメタデータを削除しています ...");
        this.onUninstalled(pluginNameFull);

        System.out.println("依存関係ツリーを構築しています ...");
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
        List<DependencyNode> dummy = Collections.emptyList();
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
        onInstalled(plugin, operator, resolveQuery, System.currentTimeMillis(), isDependency);
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

    private boolean checkNoAutoCreateMetadata(Plugin plugin)
    {
        String pluginName = plugin.getName();
        if (this.exceptedPluginModifications.contains(pluginName))
        {
            this.exceptedPluginModifications.remove(pluginName);
            return true;
        }

        return false;
    }
}
