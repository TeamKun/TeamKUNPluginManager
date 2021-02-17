package net.kunmc.lab.teamkunpluginmanager;

import net.kunmc.lab.teamkunpluginmanager.commands.CommandMain;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandUpdate;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.plugin.PluginEventListener;
import net.kunmc.lab.teamkunpluginmanager.utils.Say2Functional;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public final class TeamKunPluginManager extends JavaPlugin
{
    public static final String DATABASE_PATH = "plugins/TeamKunPluginManager/database/";
    public static TeamKunPluginManager plugin;
    public static FileConfiguration config;
    public static TokenVault vault;
    public static Say2Functional functional;
    public static boolean enableBuildTree = true;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        plugin = this;
        config = getConfig();
        functional = new Say2Functional(this);

        vault = new TokenVault();

        Bukkit.getPluginCommand("kunpluginmanager").setExecutor(new CommandMain());
        Bukkit.getPluginCommand("kunpluginmanager").setTabCompleter(new CommandMain());

        DependencyTree.initialize(TeamKunPluginManager.config.getString("dependPath"));
        DependencyTree.initializeTable();
        KnownPlugins.initialization(TeamKunPluginManager.config.getString("resolvePath"));
        new BukkitRunnable()
        {

            @Override
            public void run()
            {
                DependencyTree.wipeAllPlugin();
                getLogger().info("依存関係ツリーを構築中...");
                DependencyTree.crawlAllPlugins();
                getLogger().info("依存関係ツリーの構築完了");
                //すべてのPLが読み終わった後にイベントリスナを登録
                Bukkit.getPluginManager().registerEvents(new PluginEventListener(), TeamKunPluginManager.plugin);
            }
        }.runTaskLater(this, 1L);

        if (!new File(DATABASE_PATH).exists())
            CommandUpdate.onCommand(Bukkit.getConsoleSender(), null);

        String tokenEnv = System.getenv("TOKEN");

        if (tokenEnv != null && !tokenEnv.isEmpty())
        {
            if (!vault.getToken().equals(tokenEnv))
                vault.vault(tokenEnv);
            return;
        }
    }

    @Override
    public void onDisable()
    {
        if (DependencyTree.dataSource != null)
            DependencyTree.dataSource.close();
        if (KnownPlugins.dataSource != null)
            KnownPlugins.dataSource.close();
    }

    public boolean isTokenAvailable()
    {
        return !vault.getToken().isEmpty();
    }
}
