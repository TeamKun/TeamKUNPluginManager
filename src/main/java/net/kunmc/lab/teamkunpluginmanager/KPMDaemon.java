package net.kunmc.lab.teamkunpluginmanager;

import lombok.AccessLevel;
import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminals;
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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * KPMを操作するためのクラスです。
 */
@Getter
public class KPMDaemon
{
    @SuppressWarnings("NotNullFieldNotInitialized")
    @Getter(AccessLevel.NONE)
    @NotNull
    private static KPMDaemon INSTANCE;

    @Getter(AccessLevel.NONE)
    @NotNull
    private final TeamKunPluginManager kpmInstance;

    /**
     * ログを出力するためのロガーです。
     */
    @NotNull
    private final Logger logger;

    /**
     * データフォルダのパスです。
     */
    @NotNull
    private final Path dataFolderPath;

    /**
     * プラグインのメタデータを管理するクラスです。
     */
    @NotNull
    private final PluginMetaManager pluginMetaManager;
    /**
     * トークンを保管しセキュアに管理するクラスです。
     */
    @NotNull
    private final TokenStore tokenStore;
    /**
     * プラグインクエリを解決するクラスです。
     */
    @NotNull
    private final PluginResolver pluginResolver;
    /**
     * インストールを管理するクラスです。
     */
    @NotNull
    private final InstallManager installManager;
    /**
     * エイリアスを管理するクラスです。
     */
    @NotNull
    private final AliasProvider aliasProvider;

    /**
     * プラグインをロード/アンロードするためのクラスです。
     */
    @NotNull
    private final PluginLoader pluginLoader;

    {
        INSTANCE = this;
    }

    public KPMDaemon(
            @NotNull TeamKunPluginManager kpmInstance,
            @NotNull Logger logger,
            @NotNull Path dataFolderPath,
            @NotNull Path databasePath,
            @NotNull Path tokenPath,
            @NotNull Path tokenKeyPath,
            @NotNull Path aliasPath)
    {
        this.kpmInstance = kpmInstance;
        this.logger = logger;
        this.dataFolderPath = dataFolderPath;
        this.pluginMetaManager = new PluginMetaManager(kpmInstance, databasePath);
        this.tokenStore = new TokenStore(tokenPath, tokenKeyPath);
        this.pluginResolver = new PluginResolver();
        this.aliasProvider = new AliasProvider(aliasPath);
        this.pluginLoader = new PluginLoader();
        this.installManager = new InstallManager(this);
    }

    /**
     * KPMのインスタンスを取得します。
     *
     * @return KPMのインスタンス
     */
    @NotNull
    public static KPMDaemon getInstance()
    {
        return INSTANCE;
    }

    public void setupDaemon(@NotNull Path dataFolder)
    {
        this.setupDependencyTree(dataFolder);
        this.setupPluginResolvers();
        this.setupToken();
    }

    private void setupDependencyTree(Path dataFolder)
    {
        logger.info("Loading plugin meta data ...");
        this.pluginMetaManager.crawlAll();

        Path aliasFile = dataFolder.resolve("aliases.db");
        boolean isFirstTime = !Files.exists(aliasFile);
        if (isFirstTime && this.tokenStore.isTokenAvailable()) // Do update
            new CommandUpdate(this).onCommand(Bukkit.getConsoleSender(), Terminals.ofConsole(), new String[0]);
    }

    private void setupPluginResolvers()
    {
        GitHubURLResolver githubResolver = new GitHubURLResolver();
        this.pluginResolver.addResolver(new SpigotMCResolver(), "spigotmc", "spigot", "spiget");
        this.pluginResolver.addResolver(new CurseBukkitResolver(), "curseforge", "curse", "forge", "bukkit");
        this.pluginResolver.addResolver(new KnownPluginsResolver(this), "local", "alias");
        this.pluginResolver.addResolver(new OmittedGitHubResolver(), "github", "gh");
        this.pluginResolver.addResolver(githubResolver, "github", "gh");

        this.pluginResolver.addOnNotFoundResolver(new BruteforceGitHubResolver(
                kpmInstance.getPluginConfig().get("githubName"),
                githubResolver
        ));
    }

    private void setupToken()
    {
        try
        {
            boolean tokenAvailable = this.tokenStore.loadToken();
            if (!tokenAvailable)
                if (this.tokenStore.migrateToken())
                    tokenAvailable = true;

            if (!tokenAvailable)
                this.logger.warn("Token is not available. Please login with /kpm register");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            this.logger.error("Failed to load token");
        }


        String tokenEnv = System.getenv("TOKEN");

        if (tokenEnv != null && !tokenEnv.isEmpty())
            this.tokenStore.fromEnv();
    }

    public void shutdown()
    {
        this.pluginMetaManager.getProvider().close();
        this.aliasProvider.close();
    }
}
