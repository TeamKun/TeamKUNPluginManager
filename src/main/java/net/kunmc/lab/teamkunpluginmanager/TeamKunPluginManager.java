package net.kunmc.lab.teamkunpluginmanager;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandMain;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandUpdate;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.plugin.PluginEventListener;
import net.kunmc.lab.teamkunpluginmanager.resolver.PluginResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.BruteforceGitHubResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.CurseBukkitResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.GitHubURLResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.KnownPluginsResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.OmittedGitHubResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.SpigotMCResolver;
import net.kunmc.lab.teamkunpluginmanager.utils.Say2Functional;
import net.kunmc.lab.teamkunpluginmanager.utils.Session;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Getter
public final class TeamKunPluginManager extends JavaPlugin
{
    public static final String DATABASE_PATH = "plugins/TeamKunPluginManager/database/";
    @Getter
    private static TeamKunPluginManager plugin;

    private FileConfiguration pluginConfig;
    private TokenVault vault;
    private Say2Functional functional;
    @Setter
    private boolean enableBuildTree = true;
    private Session session;
    private PluginResolver resolver;

    private static void setupDependencyTree(TeamKunPluginManager plugin)
    {
        DependencyTree.initialize(plugin.getPluginConfig().getString("dependPath"));
        DependencyTree.initializeTable();
        KnownPlugins.initialization(plugin.getPluginConfig().getString("resolvePath"));

        if (KnownPlugins.isLegacy())
        {
            plugin.getLogger().warning("プラグイン定義ファイルの形式が古いです。更新しています...");
            KnownPlugins.migration();
            CommandUpdate.onCommand(Bukkit.getConsoleSender(), null);
        }

        Runner.runLater(() -> {
            DependencyTree.wipeAllPlugin();
            plugin.getLogger().info("依存関係ツリーを構築中...");
            DependencyTree.crawlAllPlugins();
            plugin.getLogger().info("依存関係ツリーの構築完了");

            //すべてのPLが読み終わった後にイベントリスナを登録
            Bukkit.getPluginManager().registerEvents(
                    new PluginEventListener(plugin),
                    TeamKunPluginManager.plugin
            );
        }, 1L);
    }

    private static void setupResolver(TeamKunPluginManager plugin)
    {
        PluginResolver resolver = plugin.getResolver();

        GitHubURLResolver githubResolver = new GitHubURLResolver();
        resolver.addResolver(new SpigotMCResolver(), "spigotmc", "spigot", "spiget");
        resolver.addResolver(new CurseBukkitResolver(), "curseforge", "curse", "forge", "bukkit");
        resolver.addResolver(new KnownPluginsResolver(), "local", "alias");
        resolver.addResolver(new OmittedGitHubResolver(), "github", "gh");
        resolver.addResolver(githubResolver, "github", "gh");

        resolver.addOnNotFoundResolver(new BruteforceGitHubResolver(plugin, githubResolver));
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

    @Override
    public void onEnable()
    {
        session = new Session();
        saveDefaultConfig();
        plugin = this;
        pluginConfig = getConfig();
        functional = new Say2Functional(this);
        resolver = new PluginResolver();

        setupResolver(this);

        vault = new TokenVault();

        Bukkit.getPluginCommand("kunpluginmanager").setExecutor(new CommandMain());
        Bukkit.getPluginCommand("kunpluginmanager").setTabCompleter(new CommandMain());

        setupDependencyTree(this);

        if (!new File(DATABASE_PATH).exists())
            CommandUpdate.onCommand(Bukkit.getConsoleSender(), null);

        String tokenEnv = System.getenv("TOKEN");

        if (tokenEnv != null && !tokenEnv.isEmpty())
        {
            if (!vault.getToken().equals(tokenEnv))
                vault.vault(tokenEnv);
            return;
        }

        if (vault.getToken().equals(""))
            vault.vault("");
    }

}
