package net.kunmc.lab.teamkunpluginmanager;

import lombok.AccessLevel;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.alias.AliasPluginResolver;
import net.kunmc.lab.teamkunpluginmanager.alias.AliasProvider;
import net.kunmc.lab.teamkunpluginmanager.installer.InstallManager;
import net.kunmc.lab.teamkunpluginmanager.loader.PluginLoader;
import net.kunmc.lab.teamkunpluginmanager.meta.PluginMetaManager;
import net.kunmc.lab.teamkunpluginmanager.resolver.PluginResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.BruteforceGitHubResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.CurseBukkitResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.GitHubURLResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.OmittedGitHubResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.SpigotMCResolver;
import net.kunmc.lab.teamkunpluginmanager.utils.http.Requests;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * KPMのデーモンです。
 */
@Getter
public class KPMDaemon
{
    @SuppressWarnings("NotNullFieldNotInitialized")
    @Getter(AccessLevel.NONE)
    @NotNull
    private static KPMDaemon INSTANCE;

    /**
     * KPMの環境です。
     */
    @NotNull
    private final KPMEnvironment envs;
    /**
     * KPMのロガーです。
     */
    @NotNull
    private final Logger logger;

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

    public KPMDaemon(@NotNull KPMEnvironment env)
    {
        this.envs = env;
        this.logger = env.getLogger();
        this.pluginMetaManager = new PluginMetaManager(env.getPlugin(), env.getMetadataDBPath());
        this.tokenStore = new TokenStore(env.getTokenPath(), env.getTokenKeyPath());
        this.pluginResolver = new PluginResolver();
        this.aliasProvider = new AliasProvider(env.getAliasesDBPath());
        this.pluginLoader = new PluginLoader(this);
        this.installManager = new InstallManager(this);

        this.setupDaemon(env.getDataDirPath(), env.getOrganizations());
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
        this.setupDependencyTree();
        this.setupPluginResolvers(organizationNames);
        this.setupToken();
        this.initializeRequests();
    }

    private void setupDependencyTree()
    {
        this.logger.info("Loading plugin meta data ...");
        this.pluginMetaManager.crawlAll();
    }

    private void setupPluginResolvers(List<String> organizationNames)
    {
        GitHubURLResolver githubResolver = new GitHubURLResolver();
        this.pluginResolver.addResolver(new SpigotMCResolver(), "spigotmc", "spigot", "spiget");
        this.pluginResolver.addResolver(new CurseBukkitResolver(), "curseforge", "curse", "forge", "bukkit");
        this.pluginResolver.addResolver(new AliasPluginResolver(this), "local", "alias");
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

    private void initializeRequests()
    {
        Requests.setVersion(this.getVersion());
        Requests.setTokenStore(this.tokenStore);
    }

    public void shutdown()
    {
        this.pluginMetaManager.getProvider().close();
        this.aliasProvider.close();
    }

    public String getVersion()
    {
        return this.getEnvs().getPlugin().getDescription().getVersion();
    }
}
