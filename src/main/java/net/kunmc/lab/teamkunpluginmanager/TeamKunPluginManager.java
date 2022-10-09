package net.kunmc.lab.teamkunpluginmanager;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandManager;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminals;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandAutoRemove;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandClean;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandDebug;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandInfo;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandInstall;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandRegister;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandReload;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandResolve;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandStatus;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandUninstall;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandUpdate;
import net.kunmc.lab.teamkunpluginmanager.plugin.alias.AliasProvider;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.loader.PluginLoader;
import net.kunmc.lab.teamkunpluginmanager.plugin.meta.PluginMetaManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.PluginResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl.BruteforceGitHubResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl.CurseBukkitResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl.GitHubURLResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl.KnownPluginsResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl.OmittedGitHubResolver;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.impl.SpigotMCResolver;
import net.kunmc.lab.teamkunpluginmanager.utils.TokenStore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
public final class TeamKunPluginManager extends JavaPlugin
{
    @Getter
    private static TeamKunPluginManager plugin;

    private FileConfiguration pluginConfig;
    private TokenStore tokenStore;

    private PluginResolver resolver;
    private CommandManager commandManager;
    private InstallManager installManager;
    private PluginMetaManager pluginMetaManager;
    private AliasProvider aliasProvider;

    private void setupDependencyTree()
    {
        this.pluginMetaManager = new PluginMetaManager(
                this,
                Paths.get(plugin.getDataFolder().toURI()).resolve("plugins.db")
        );

        System.out.println("プラグインメタデータを取得中 ...");
        this.pluginMetaManager.crawlAll();

        Path aliasFile = Paths.get(plugin.getDataFolder().toURI()).resolve("aliases.db");
        boolean isFirstTime = !Files.exists(aliasFile);

        this.aliasProvider = new AliasProvider(aliasFile);

        if (isFirstTime && isTokenAvailable()) // Do update
            new CommandUpdate().onCommand(Bukkit.getConsoleSender(), Terminals.ofConsole(), new String[0]);
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
        this.pluginMetaManager.getProvider().close();
        this.aliasProvider.close();
    }

    public boolean isTokenAvailable()
    {
        return tokenStore.isTokenAvailable();
    }

    public static void registerCommands(CommandManager commandManager)
    {
        commandManager.registerCommand("autoremove", new CommandAutoRemove());
        commandManager.registerCommand("clean", new CommandClean());
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

    private void setupToken()
    {
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


        String tokenEnv = System.getenv("TOKEN");

        if (tokenEnv != null && !tokenEnv.isEmpty())
            tokenStore.fromEnv();

    }

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        plugin = this;
        pluginConfig = getConfig();
        resolver = new PluginResolver();
        commandManager = new CommandManager(this, "kunpluginmanager", "TeamKUNPluginManager", "kpm");
        installManager = new InstallManager(this);
        new PluginLoader(); // Initialize plugin loader

        setupResolver(this);
        setupToken();
        setupDependencyTree();

        registerCommands(commandManager);
    }

}
