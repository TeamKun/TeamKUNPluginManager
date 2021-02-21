package net.kunmc.lab.teamkunpluginmanager.plugin;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class PluginEventListener implements Listener
{
    @EventHandler
    public void onEnable(PluginEnableEvent e)
    {
        if (!TeamKunPluginManager.enableBuildTree)
            return;
        TeamKunPluginManager.plugin.getLogger().info("依存関係ツリーを構築中...");
        DependencyTree.crawlPlugin(e.getPlugin());
        TeamKunPluginManager.plugin.getLogger().info("依存関係ツリーの構築完了");
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e)
    {
        if (!TeamKunPluginManager.enableBuildTree)
            return;
        new BukkitRunnable()
        {

            @Override
            public void run()
            {
                File f = PluginUtil.getFile(e.getPlugin());
                if (f == null || !f.exists())
                {
                    TeamKunPluginManager.plugin.getLogger().info("依存関係ツリーを構築中...");
                    DependencyTree.wipePlugin(e.getPlugin());
                    TeamKunPluginManager.plugin.getLogger().info("依存関係ツリーの構築完了");
                }
            }
        }.runTaskLater(TeamKunPluginManager.plugin, 15L);
    }
}
