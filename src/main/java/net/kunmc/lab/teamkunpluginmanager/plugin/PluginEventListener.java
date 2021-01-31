package net.kunmc.lab.teamkunpluginmanager.plugin;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class PluginEventListener implements Listener
{
    @EventHandler
    public void onEnable(PluginEnableEvent e)
    {
        TeamKunPluginManager.plugin.getLogger().info("依存関係ツリーを構築中...");
        DependencyTree.crawlPlugin(e.getPlugin());
        TeamKunPluginManager.plugin.getLogger().info("依存関係ツリーの構築完了");
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e)
    {
        TeamKunPluginManager.plugin.getLogger().info("依存関係ツリーを構築中...");
        DependencyTree.wipePlugin(e.getPlugin());
        TeamKunPluginManager.plugin.getLogger().info("依存関係ツリーの構築完了");
    }
}
