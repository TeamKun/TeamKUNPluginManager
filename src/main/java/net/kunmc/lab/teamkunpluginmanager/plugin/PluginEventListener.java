package net.kunmc.lab.teamkunpluginmanager.plugin;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import java.io.File;

@AllArgsConstructor
public class PluginEventListener implements Listener
{
    private final TeamKunPluginManager kpmInstance;

    @EventHandler
    public void onEnable(PluginEnableEvent e)
    {
        if (!kpmInstance.isEnableBuildTree())
            return;
        kpmInstance.getLogger().info("依存関係ツリーを構築中(ADD:" + e.getPlugin().getName() + ")...");
        DependencyTree.crawlPlugin(e.getPlugin());
        kpmInstance.getLogger().info("依存関係ツリーの構築完了");
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e)
    {
        if (!kpmInstance.isEnableBuildTree())
            return;

        Runner.runLater(() -> {
            File f = PluginUtil.getFile(e.getPlugin());
            if (f == null || !f.exists())
            {
                kpmInstance.getLogger().info("依存関係ツリーを構築中(RMV:" + e.getPlugin().getName() + ")...");
                DependencyTree.wipePlugin(e.getPlugin());
                kpmInstance.getLogger().info("依存関係ツリーの構築完了");
            }
        }, 2L);
    }
}
