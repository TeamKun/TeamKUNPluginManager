package net.kunmc.lab.kpm;

import lombok.AccessLevel;
import lombok.Getter;
import net.kunmc.lab.kpm.alias.AliasPluginResolver;
import net.kunmc.lab.kpm.alias.AliasProvider;
import net.kunmc.lab.kpm.hook.HookExecutor;
import net.kunmc.lab.kpm.installer.InstallManager;
import net.kunmc.lab.kpm.kpminfo.KPMInfoManager;
import net.kunmc.lab.kpm.loader.PluginLoader;
import net.kunmc.lab.kpm.meta.InstallOperator;
import net.kunmc.lab.kpm.meta.PluginMeta;
import net.kunmc.lab.kpm.meta.PluginMetaIterator;
import net.kunmc.lab.kpm.meta.PluginMetaManager;
import net.kunmc.lab.kpm.resolver.PluginResolver;
import net.kunmc.lab.kpm.resolver.impl.BruteforceGitHubResolver;
import net.kunmc.lab.kpm.resolver.impl.CurseBukkitResolver;
import net.kunmc.lab.kpm.resolver.impl.GitHubURLResolver;
import net.kunmc.lab.kpm.resolver.impl.OmittedGitHubResolver;
import net.kunmc.lab.kpm.resolver.impl.SpigotMCResolver;
import net.kunmc.lab.kpm.utils.http.Requests;
import net.kunmc.lab.kpm.utils.versioning.Version;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
     * プラグインのKPM情報ファイルを管理するクラスです。
     */
    @NotNull
    private final KPMInfoManager kpmInfoManager;
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
    /**
     * フックを実行するクラスです。
     */
    @NotNull
    private final HookExecutor hookExecutor;

    {
        INSTANCE = this;
    }

    public KPMDaemon(@NotNull KPMEnvironment env)
    {
        this.envs = env;
        this.logger = env.getLogger();
        this.pluginMetaManager = new PluginMetaManager(env.getPlugin(), env.getMetadataDBPath());
        this.kpmInfoManager = new KPMInfoManager(this);
        this.tokenStore = new TokenStore(env.getTokenPath(), env.getTokenKeyPath());
        this.pluginResolver = new PluginResolver();
        this.aliasProvider = new AliasProvider(env.getAliasesDBPath());
        this.pluginLoader = new PluginLoader(this);
        this.installManager = new InstallManager(this);
        this.hookExecutor = new HookExecutor(this);

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

        List<Plugin> plugins = Arrays.asList(Bukkit.getPluginManager().getPlugins());
        List<String> pluginNames = plugins.stream().parallel()
                .map(Plugin::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());


        try (PluginMetaIterator iterator = this.pluginMetaManager.getProvider().getPluginMetaIterator())
        {
            while (iterator.hasNext())
            {
                PluginMeta meta = iterator.next();

                if (pluginNames.contains(meta.getName().toLowerCase()))
                    continue;


                this.logger.info("Plugin {} is not loaded by server. Removing from database ...", meta.getName());
                iterator.remove();
            }
        }

        for (Plugin plugin : plugins)
        {
            if (this.pluginMetaManager.hasPluginMeta(plugin))
                continue;

            this.pluginMetaManager.getProvider().savePluginMeta(
                    plugin,
                    InstallOperator.UNKNOWN,
                    System.currentTimeMillis(),
                    null,
                    false
            );

            this.logger.info("Plugin {} is not managed by KPM. Adding to database ...", plugin.getName());

            this.pluginMetaManager.getProvider().buildDependencyTree(plugin);
        }

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
        Requests.setVersion(this.getVersion().toString());
        Requests.setTokenStore(this.tokenStore);
    }

    public void shutdown()
    {
        this.pluginMetaManager.getProvider().close();
        this.aliasProvider.close();
    }

    public Version getVersion()
    {
        return Version.of(this.getEnvs().getPlugin().getDescription().getVersion());
    }
}
