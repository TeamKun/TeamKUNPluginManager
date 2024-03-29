package org.kunlab.kpm;

import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.alias.AliasProviderImpl;
import org.kunlab.kpm.alias.interfaces.AliasProvider;
import org.kunlab.kpm.hook.HookExecutorImpl;
import org.kunlab.kpm.hook.interfaces.HookExecutor;
import org.kunlab.kpm.http.Requests;
import org.kunlab.kpm.installer.InstallManagerImpl;
import org.kunlab.kpm.installer.interfaces.InstallManager;
import org.kunlab.kpm.installer.interfaces.loader.PluginLoader;
import org.kunlab.kpm.interfaces.KPMEnvironment;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.kpminfo.KPMInfoManagerImpl;
import org.kunlab.kpm.kpminfo.interfaces.KPMInfoManager;
import org.kunlab.kpm.meta.PluginMetaManagerImpl;
import org.kunlab.kpm.meta.interfaces.PluginMetaManager;
import org.kunlab.kpm.resolver.PluginResolverImpl;
import org.kunlab.kpm.resolver.impl.AliasPluginResolver;
import org.kunlab.kpm.resolver.impl.CurseBukkitResolver;
import org.kunlab.kpm.resolver.impl.RawURLResolver;
import org.kunlab.kpm.resolver.impl.SpigotMCResolver;
import org.kunlab.kpm.resolver.impl.github.BruteforceGitHubResolver;
import org.kunlab.kpm.resolver.impl.github.GitHubURLResolver;
import org.kunlab.kpm.resolver.impl.github.OmittedGitHubResolver;
import org.kunlab.kpm.resolver.interfaces.PluginResolver;
import org.kunlab.kpm.task.loader.PluginLoaderImpl;
import org.kunlab.kpm.utils.APIUtils;
import org.kunlab.kpm.utils.interfaces.ServerConditionChecker;
import org.kunlab.kpm.versioning.Version;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * KPMのデーモンです。
 */
public class KPMDaemon implements KPMRegistry
{
    private final Logger logger;
    private final ExceptionHandler exceptionHandler;
    private final KPMEnvironment environment;
    private final AliasProvider aliasProvider;
    private final PluginMetaManager pluginMetaManager;
    private final KPMInfoManager kpmInfoManager;
    private final HookExecutor hookExecutor;
    private final TokenStore tokenStore;
    private final InstallManager installManager;
    private final PluginLoader pluginLoader;
    private final PluginResolver pluginResolver;
    private final ServerConditionChecker serverConditionChecker;

    public KPMDaemon(@NotNull KPMEnvironment env)
    {
        this.logger = env.getLogger();
        this.environment = env;
        this.exceptionHandler = env.getExceptionHandler();

        this.pluginMetaManager = new PluginMetaManagerImpl(
                this,
                env.getMetadataDBPath(),
                env.getPlugin()
        );
        this.aliasProvider = new AliasProviderImpl(env.getAliasesDBPath());
        this.kpmInfoManager = new KPMInfoManagerImpl(this);
        this.hookExecutor = new HookExecutorImpl(this);
        this.tokenStore = new TokenStore(env.getTokenPath(), env.getTokenKeyPath(), this.exceptionHandler);
        this.installManager = new InstallManagerImpl(this.tokenStore);
        this.pluginLoader = new PluginLoaderImpl(this);
        this.pluginResolver = new PluginResolverImpl();
        this.serverConditionChecker = getServerConditionChecker(APIUtils.getAPIVersion());

        this.setupDaemon(env.getOrganizations());
    }

    private static ServerConditionChecker getServerConditionChecker(String apiVersion)
    {
        switch (apiVersion)
        {
            case "1.16":
                return new org.kunlab.kpm.nms.v1_16.utils.ServerConditionCheckerImpl();
            case "1.19":
                return new org.kunlab.kpm.nms.v1_19.utils.ServerConditionCheckerImpl();
        }

        throw new IllegalStateException("Unsupported API version: " + apiVersion);
    }

    public void setupDaemon(@NotNull List<String> organizationNames)
    {
        this.setupPluginResolvers(organizationNames);
        this.setupToken();
        this.initializeRequests();
        this.loadKPMInformationFromPlugins();

        // サーバが全て読み込まれた後にプラグインをクロールする。
        Runner.runLater(() -> this.getPluginMetaManager().crawlAll(), 1L);
    }

    private void loadKPMInformationFromPlugins()
    {
        Runner.runLater(() -> {

            this.logger.info("Loading KPM information from plugins...");

            KPMInfoManager kpmInfoManager = this.getKpmInfoManager();
            Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

            long loaded = Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                    .map(kpmInfoManager::getOrLoadInfo)
                    .filter(Objects::nonNull)
                    .count();

            this.logger.log(Level.INFO, "Loaded {0} KPM information from {1} plugins.", new Object[]{loaded, plugins.length});
        }, 1L);
    }

    private void setupPluginResolvers(List<String> organizationNames)
    {
        PluginResolver resolver = this.getPluginResolver();

        GitHubURLResolver githubResolver = new GitHubURLResolver();
        resolver.addResolver(new AliasPluginResolver(this), "local", "alias");
        resolver.addResolver(new SpigotMCResolver(), "spigotmc", "spigot", "spiget");
        resolver.addResolver(new CurseBukkitResolver(), "curseforge", "curse", "forge", "bukkit");
        resolver.addResolver(new OmittedGitHubResolver(githubResolver), "github", "gh");
        resolver.addResolver(githubResolver, "github", "gh");

        resolver.addFallbackResolver(new BruteforceGitHubResolver(
                organizationNames,
                githubResolver
        ));

        resolver.addFallbackResolver(new RawURLResolver());
    }

    private void setupToken()
    {
        TokenStore tokenStore = this.getTokenStore();

        try
        {
            boolean tokenAvailable = tokenStore.loadToken();
            if (!(tokenAvailable || tokenStore.migrateToken()))
                this.logger.log(Level.WARNING, "No token is available in this server. Please run /kpm token to set a token.");

        }
        catch (IOException e)
        {
            this.getExceptionHandler().report(e);
            this.logger.log(Level.WARNING, "Failed to load token.");
        }


        String tokenEnv = System.getenv("TOKEN");

        if (tokenEnv != null && !tokenEnv.isEmpty())
            tokenStore.fromEnv();
    }

    private void initializeRequests()
    {
        Requests.setTokenStore(this.getTokenStore());

        Requests.setRedirectLimit(this.environment.getHTTPMaxRedirects());
        Requests.setConnectTimeout(this.environment.getHTTPTimeout());
        Requests.getExtraHeaders().put("User-Agent", this.environment.getHTTPUserAgent()
                .replace("%productName%", this.environment.getPlugin().getName())
                .replace("%productVersion%", this.getVersion().toString()));
    }

    @Override
    public void shutdown()
    {
        this.getPluginMetaManager().getProvider().close();
        this.getAliasProvider().close();
    }

    public Version getVersion()
    {
        return Version.of(this.getEnvironment().getPlugin().getDescription().getVersion());
    }

    public Logger getLogger()
    {
        return this.logger;
    }

    public ExceptionHandler getExceptionHandler()
    {
        return this.exceptionHandler;
    }

    public KPMEnvironment getEnvironment()
    {
        return this.environment;
    }

    public AliasProvider getAliasProvider()
    {
        return this.aliasProvider;
    }

    public PluginMetaManager getPluginMetaManager()
    {
        return this.pluginMetaManager;
    }

    public KPMInfoManager getKpmInfoManager()
    {
        return this.kpmInfoManager;
    }

    public HookExecutor getHookExecutor()
    {
        return this.hookExecutor;
    }

    public TokenStore getTokenStore()
    {
        return this.tokenStore;
    }

    public InstallManager getInstallManager()
    {
        return this.installManager;
    }

    public PluginLoader getPluginLoader()
    {
        return this.pluginLoader;
    }

    public PluginResolver getPluginResolver()
    {
        return this.pluginResolver;
    }

    public ServerConditionChecker getServerConditionChecker()
    {
        return this.serverConditionChecker;
    }
}
