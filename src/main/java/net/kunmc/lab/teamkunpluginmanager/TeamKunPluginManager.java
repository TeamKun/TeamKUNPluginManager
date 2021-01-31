package net.kunmc.lab.teamkunpluginmanager;

import net.kunmc.lab.teamkunpluginmanager.commands.CommandMain;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.PluginEventListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class TeamKunPluginManager extends JavaPlugin
{
    public static TeamKunPluginManager plugin;
    public static FileConfiguration config;
    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        plugin = this;
        config = getConfig();
        Bukkit.getPluginCommand("kunpluginmanager").setExecutor(new CommandMain());

        if (config.getString("oauth", "").equals(""))
        {
            System.out.println("Please set OAuth token in config.yml");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        DependencyTree.initialize();
        DependencyTree.initializeTable();
        new BukkitRunnable()
        {

            @Override
            public void run()
            {
                getLogger().info("依存関係ツリーを構築中...");
                DependencyTree.crawlAllPlugins();
                getLogger().info("依存関係ツリーの構築完了");
                //すべてのPLが読み終わった後にイベントリスナを登録
                Bukkit.getPluginManager().registerEvents(new PluginEventListener(), TeamKunPluginManager.plugin);
            }
        }.runTaskLater(this, 1L);
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }
}
