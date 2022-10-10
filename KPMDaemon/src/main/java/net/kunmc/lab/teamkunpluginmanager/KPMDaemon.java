package net.kunmc.lab.teamkunpluginmanager;

import lombok.AccessLevel;
import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminals;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.alias.AliasProvider;
import net.kunmc.lab.teamkunpluginmanager.installer.InstallManager;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.update.UpdateArgument;
import net.kunmc.lab.teamkunpluginmanager.loader.PluginLoader;
import net.kunmc.lab.teamkunpluginmanager.meta.PluginMetaManager;
import net.kunmc.lab.teamkunpluginmanager.resolver.PluginResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.BruteforceGitHubResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.CurseBukkitResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.GitHubURLResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.KnownPluginsResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.OmittedGitHubResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.SpigotMCResolver;
import net.kunmc.lab.teamkunpluginmanager.utils.TokenStore;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

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

    @NotNull
    private final Plugin plugin;

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
     * エイリアスのソースです。
     */
    @NotNull
    private final HashMap<String, String> sources;

    /**
     * プラグインをロード/アンロードするためのクラスです。
     */
    @NotNull
    private final PluginLoader pluginLoader;

    {
        INSTANCE = this;
    }

    public KPMDaemon(
            @NotNull Plugin plugin,
            @NotNull Logger logger,
            @NotNull Path dataFolderPath,
            @NotNull Path databasePath,
            @NotNull Path tokenPath,
            @NotNull Path tokenKeyPath,
            @NotNull Path aliasPath,
            @NotNull List<String> organizationNames,
            @NotNull HashMap<String, String> sources)
    {
        this.plugin = plugin;
        this.logger = logger;
        this.dataFolderPath = dataFolderPath;
        this.pluginMetaManager = new PluginMetaManager(plugin, databasePath);
        this.tokenStore = new TokenStore(tokenPath, tokenKeyPath);
        this.pluginResolver = new PluginResolver();
        this.aliasProvider = new AliasProvider(aliasPath);
        this.pluginLoader = new PluginLoader(this);
        this.installManager = new InstallManager(this);
        this.sources = sources;

        this.setupDaemon(dataFolderPath, organizationNames);
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

    public void setupDaemon(@NotNull Path dataFolder, @NotNull List<String> organizationNames)
    {
        this.setupDependencyTree(dataFolder);
        this.setupPluginResolvers(organizationNames);
        this.setupToken();
    }

    private void firstUpdate()
    {
        Runner.runAsync(() -> {
            this.getInstallManager().runUpdate(
                    Terminals.ofConsole(),
                    new UpdateArgument(this.sources)
            );
        });
    }

    private void setupDependencyTree(Path dataFolder)
    {
        logger.info("Loading plugin meta data ...");
        this.pluginMetaManager.crawlAll();

        Path aliasFile = dataFolder.resolve("aliases.db");
        boolean isFirstTime = !Files.exists(aliasFile);
        if (isFirstTime && this.tokenStore.isTokenAvailable())
            this.firstUpdate();
    }

    private void setupPluginResolvers(List<String> organizationNames)
    {
        GitHubURLResolver githubResolver = new GitHubURLResolver();
        this.pluginResolver.addResolver(new SpigotMCResolver(), "spigotmc", "spigot", "spiget");
        this.pluginResolver.addResolver(new CurseBukkitResolver(), "curseforge", "curse", "forge", "bukkit");
        this.pluginResolver.addResolver(new KnownPluginsResolver(this), "local", "alias");
        this.pluginResolver.addResolver(new OmittedGitHubResolver(), "github", "gh");
        this.pluginResolver.addResolver(githubResolver, "github", "gh");

        this.pluginResolver.addOnNotFoundResolver(new BruteforceGitHubResolver(
                organizationNames,
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

    public String getVersion()
    {
        return this.plugin.getDescription().getVersion();
    }
}
