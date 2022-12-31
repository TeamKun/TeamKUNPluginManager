package net.kunmc.lab.kpm.meta;

import lombok.Getter;
import net.kunmc.lab.kpm.KPMRegistry;
import net.kunmc.lab.kpm.interfaces.meta.PluginMetaManager;
import net.kunmc.lab.kpm.utils.ServerConditionChecker;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PluginMetaManagerImpl implements PluginMetaManager
{
    private final KPMRegistry registry;

    @Getter
    private final PluginMetaProviderImpl provider;
    private final List<String> exceptedPluginModifications;

    public PluginMetaManagerImpl(@NotNull KPMRegistry registry, @NotNull Path metadataPath, @NotNull Plugin plugin)
    {
        this.registry = registry;
        this.provider = new PluginMetaProviderImpl(plugin, metadataPath);
        this.exceptedPluginModifications = new ArrayList<>();

        // Below lambda will be executed after all plugins are loaded.
        // (Bukkit runs task after all plugins are loaded.)
        Runner.runLater(() -> Bukkit.getPluginManager().registerEvents(this, plugin), 1L);
    }

    @Override
    public void preparePluginModify(@NotNull String pluginName)
    {
        synchronized (this.exceptedPluginModifications)
        {
            if (!this.exceptedPluginModifications.contains(pluginName))
                this.exceptedPluginModifications.add(pluginName);
        }
    }

    @Override
    public void preparePluginModify(@NotNull Plugin plugin)
    {
        this.preparePluginModify(plugin.getName());
    }

    private boolean shouldNotManage(String pluginName)
    {
        if (pluginName.equalsIgnoreCase("TeamKUNPluginManager") || pluginName.equalsIgnoreCase("KPMUpgrader"))
            return true;

        return this.checkNoAutoCreateMetadata(pluginName);
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event)
    {
        Plugin plugin = event.getPlugin();
        if (this.shouldNotManage(plugin.getName()))
            return;

        String pluginNameFull = plugin.getName() + " (" + plugin.getDescription().getVersion() + ")";

        this.registry.getLogger().info("プラグイン " + pluginNameFull + " のメタデータを作成しています ...");
        this.onInstalled(plugin, InstallOperator.UNKNOWN, null, false);

        this.registry.getLogger().info("依存関係ツリーを構築しています ...");
        this.provider.buildDependencyTree(plugin);

    }

    @EventHandler
    public void onDisable(PluginDisableEvent event)
    {
        ServerConditionChecker conditions = this.registry.getServerConditionChecker();
        if (conditions.isStopping() || conditions.isReloading())
            return;  // The server is being stopped.

        Plugin plugin = event.getPlugin();
        if (this.shouldNotManage(plugin.getName()))
            return;

        String pluginNameFull = plugin.getName() + " (" + plugin.getDescription().getVersion() + ")";

        this.registry.getLogger().info("プラグイン " + pluginNameFull + " のメタデータを削除しています ...");
        this.onUninstalled(plugin.getName());

        this.registry.getLogger().info("依存関係ツリーを構築しています ...");
        this.provider.deleteFromDependencyTree(plugin.getName());
    }

    @Override
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

    @Override
    public void onInstalled(@NotNull Plugin plugin, @NotNull InstallOperator operator, @Nullable String resolveQuery, boolean isDependency)
    {
        this.onInstalled(plugin, operator, resolveQuery, System.currentTimeMillis(), isDependency);
    }

    @Override
    public void onUninstalled(@NotNull String pluginName)
    {
        this.provider.removePluginMeta(pluginName);
        this.provider.deleteFromDependencyTree(pluginName);
    }

    @Override
    public boolean hasPluginMeta(@NotNull String pluginName)
    {
        return this.provider.isPluginMetaExists(pluginName);
    }

    @Override
    public boolean hasPluginMeta(@NotNull Plugin plugin)
    {
        return this.hasPluginMeta(plugin.getName());
    }

    private boolean checkNoAutoCreateMetadata(String pluginName)
    {
        synchronized (this.exceptedPluginModifications)
        {
            return this.exceptedPluginModifications.remove(pluginName);
        }
    }
}
