package net.kunmc.lab.teamkunpluginmanager.plugin.meta;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
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

public class PluginMetaManager implements Listener
{
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

    public void preparePluginModify(@NotNull String pluginName)
    {
        this.exceptedPluginModifications.add(PluginUtil.normalizePluginName(pluginName));
    }

    public void preparePluginModify(@NotNull Plugin plugin)
    {
        this.exceptedPluginModifications.add(PluginUtil.normalizePluginName(plugin.getName()));
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
        this.provider.buildDependencyTree(plugin);

    }

    @EventHandler
    public void onDisable(PluginDisableEvent event)
    {
        Plugin plugin = event.getPlugin();
        if (this.isNoAutoCreateMetadata(plugin))
            return;

        String normalized = PluginUtil.normalizePluginName(plugin.getName());
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
        this.provider.thinDependencyTree(pluginNameFull);
    }

    public void onInstalled(@NotNull Plugin plugin, @NotNull InstallOperator operator, @Nullable String resolveQuery, long installedAt)
    {
        this.provider.savePluginData(plugin, false);

        List<DependencyNode> dummy = Collections.emptyList();
        this.provider.savePluginMeta(
                new PluginMeta(
                        PluginUtil.normalizePluginName(plugin.getName()),
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
        this.provider.removePluginData(pluginName, false);
        this.provider.removePluginMeta(pluginName);
    }

    private boolean isNoAutoCreateMetadata(Plugin plugin)
    {
        String normalized = PluginUtil.normalizePluginName(plugin.getName());
        if (this.exceptedPluginModifications.contains(normalized))
        {
            this.exceptedPluginModifications.remove(normalized);
            return true;
        }

        return false;
    }
}
