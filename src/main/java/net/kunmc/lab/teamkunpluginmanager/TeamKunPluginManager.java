package net.kunmc.lab.teamkunpluginmanager;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandManager;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminals;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandAutoRemove;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandClean;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandDebug;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandFix;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandInfo;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandInstall;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandRegister;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandReload;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandResolve;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandStatus;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandUninstall;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandUpdate;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.plugin.PluginEventListener;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.loader.PluginLoader;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.PluginResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl.BruteforceGitHubResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl.CurseBukkitResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl.GitHubURLResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl.KnownPluginsResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl.OmittedGitHubResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl.SpigotMCResolver;
import net.kunmc.lab.teamkunpluginmanager.utils.Session;
import net.kunmc.lab.teamkunpluginmanager.utils.TokenStore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

@Getter
public final class TeamKunPluginManager extends JavaPlugin
{
    public static final String DATABASE_PATH = "plugins/TeamKunPluginManager/database/";
    @Getter
    private static TeamKunPluginManager plugin;

    private FileConfiguration pluginConfig;
    private TokenStore tokenStore;

    @Setter
    private boolean enableBuildTree = true;
    private Session session;
    private PluginResolver resolver;
    private CommandManager commandManager;
    private InstallManager installManager;

    private static void setupDependencyTree(TeamKunPluginManager plugin)
    {
        DependencyTree.initialize(plugin.getPluginConfig().getString("dependPath"));
        DependencyTree.initializeTable();
        KnownPlugins.initialization(plugin.getPluginConfig().getString("resolvePath"));

        if (KnownPlugins.isLegacy())
        {
            plugin.getLogger().warning("プラグイン定義ファイルの形式が古いです。更新しています...");
            KnownPlugins.migration();
            new CommandUpdate().onCommand(Bukkit.getConsoleSender(), Terminals.ofConsole(), new String[0]);
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
        resolver.addResolver(new KnownPluginsResolver(resolver), "local", "alias");
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
        return tokenStore.isTokenAvailable();
    }

    public static void registerCommands(CommandManager commandManager)
    {
        commandManager.registerCommand("autoremove", new CommandAutoRemove());
        commandManager.registerCommand("clean", new CommandClean());
        commandManager.registerCommand("fix", new CommandFix());
        commandManager.registerCommand("info", new CommandInfo());
        commandManager.registerCommand("install", new CommandInstall(), "add", "i");
        commandManager.registerCommand("register", new CommandRegister(), "login");
        commandManager.registerCommand("reload", new CommandReload());
        commandManager.registerCommand("resolve", new CommandResolve());
        commandManager.registerCommand("status", new CommandStatus());
        commandManager.registerCommand("uninstall", new CommandUninstall(), "remove", "rm");
        commandManager.registerCommand("update", new CommandUpdate());
        commandManager.registerCommand("debug", new CommandDebug());
    }

    @Override
    public void onEnable()
    {
        session = new Session();
        saveDefaultConfig();
        plugin = this;
        pluginConfig = getConfig();
        resolver = new PluginResolver();
        commandManager = new CommandManager(this, "kunpluginmanager", "TeamKUNPluginManager", "kpm");
        installManager = new InstallManager(this);
        new PluginLoader(); // Initialize plugin loader

        registerCommands(commandManager);

        setupResolver(this);

        tokenStore = new TokenStore(
                this.getDataFolder().toPath().resolve("token.dat"),
                this.getDataFolder().toPath().resolve("token_key.dat")
        );
        try
        {
            boolean tokenAvailable = tokenStore.loadToken();
            if (!tokenAvailable)
                if (tokenStore.migrateToken())
                    tokenAvailable = true;

            if (!tokenAvailable)
                System.out.println("Tokenが見つかりませんでした。/kpm registerでTokenを登録してください。");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("トークンの読み込みに失敗しました。");
        }

        setupDependencyTree(this);

        if (!new File(DATABASE_PATH).exists())
            new CommandUpdate().onCommand(Bukkit.getConsoleSender(), Terminals.ofConsole(), new String[0]);

        String tokenEnv = System.getenv("TOKEN");

        if (tokenEnv != null && !tokenEnv.isEmpty())
            tokenStore.fromEnv();
    }

}
