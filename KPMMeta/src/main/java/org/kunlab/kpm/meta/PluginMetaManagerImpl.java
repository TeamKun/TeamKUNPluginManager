package org.kunlab.kpm.meta;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.DebugConstants;
import org.kunlab.kpm.enums.metadata.InstallOperator;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.interfaces.meta.PluginMetaIterator;
import org.kunlab.kpm.interfaces.meta.PluginMetaManager;
import org.kunlab.kpm.utils.ServerConditionChecker;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
            {
                if (DebugConstants.PLUGIN_META_OPERATION_TRACE)
                    System.out.println("The plugin " + pluginName + " is will be modified.");

                this.exceptedPluginModifications.add(pluginName);
            }
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

    @Override
    public void crawlAll()
    {
        List<Plugin> plugins = Arrays.asList(Bukkit.getPluginManager().getPlugins());

        this.crawlRemovedPlugins(plugins);
        this.crawlAddedPlugins(plugins);
    }

    private void crawlRemovedPlugins(List<Plugin> plugins)
    {
        List<String> pluginNames = plugins.stream().parallel()
                .map(Plugin::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        try (PluginMetaIterator iterator = this.provider.getPluginMetaIterator())
        {
            while (iterator.hasNext())
            {
                PluginMeta meta = iterator.next();

                if (pluginNames.contains(meta.getName().toLowerCase()))
                    continue;

                this.registry.getLogger().log(Level.INFO, "プラグイン {0} のメタデータが見つかりましたが、" +
                                "プラグインが存在していません。メタデータを削除しています ...",
                        meta.getName()
                );
                iterator.remove();
            }
        }
    }

    private void crawlAddedPlugins(List<Plugin> plugins)
    {
        for (Plugin plugin : plugins)
        {
            if (this.hasPluginMeta(plugin) || this.shouldNotManage(plugin.getName()))
                continue;

            this.provider.savePluginMeta(
                    plugin,
                    InstallOperator.UNKNOWN,
                    System.currentTimeMillis(),
                    null,
                    false
            );
            this.registry.getLogger().log(Level.INFO, "プラグイン {0} は KPM によってインストールされていません。メタデータを構築中 ...", plugin.getName());
            this.provider.buildDependencyTree(plugin);
        }
    }

    private boolean checkNoAutoCreateMetadata(String pluginName)
    {
        synchronized (this.exceptedPluginModifications)
        {
            return this.exceptedPluginModifications.remove(pluginName);
        }
    }
}
