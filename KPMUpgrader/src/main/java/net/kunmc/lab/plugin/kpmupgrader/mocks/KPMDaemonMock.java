package net.kunmc.lab.plugin.kpmupgrader.mocks;

import lombok.Getter;
import net.kunmc.lab.kpm.TokenStore;
import net.kunmc.lab.kpm.http.Requests;
import net.kunmc.lab.kpm.interfaces.KPMEnvironment;
import net.kunmc.lab.kpm.interfaces.KPMRegistry;
import net.kunmc.lab.kpm.interfaces.alias.AliasProvider;
import net.kunmc.lab.kpm.interfaces.hook.HookExecutor;
import net.kunmc.lab.kpm.interfaces.installer.InstallManager;
import net.kunmc.lab.kpm.interfaces.installer.loader.PluginLoader;
import net.kunmc.lab.kpm.interfaces.kpminfo.KPMInfoManager;
import net.kunmc.lab.kpm.interfaces.meta.PluginMetaManager;
import net.kunmc.lab.kpm.interfaces.resolver.PluginResolver;
import net.kunmc.lab.kpm.resolver.PluginResolverImpl;
import net.kunmc.lab.kpm.resolver.impl.github.GitHubURLResolver;
import net.kunmc.lab.kpm.task.PluginLoaderImpl;
import net.kunmc.lab.kpm.utils.ServerConditionChecker;
import net.kunmc.lab.kpm.versioning.Version;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.logging.Logger;

@Getter
public class KPMDaemonMock implements KPMRegistry
{
    private final KPMEnvironment environment;
    private final Logger logger;
    private final PluginResolver pluginResolver;
    private final TokenStore tokenStore;
    private final PluginLoader loader;

    public KPMDaemonMock(@NotNull KPMEnvironment env)
    {
        this.environment = env;

        this.logger = env.getLogger();
        this.pluginResolver = new PluginResolverImpl();
        this.tokenStore = new TokenStore(env.getTokenPath(), env.getTokenKeyPath());
        this.loader = new PluginLoaderImpl(this);
        this.init();
    }

    private void init()
    {
        this.getPluginResolver().addResolver(new GitHubURLResolver(), "$");

        Requests.getExtraHeaders().put(
                "User-Agent",
                "TeamKUNPluginManager+Upgrader/" + this.getVersion()
        );
        Requests.setTokenStore(this.getTokenStore());

        try
        {
            if (!this.getTokenStore().loadToken())
            {
                String tokenEnv = System.getenv("TOKEN");

                if (tokenEnv == null || tokenEnv.isEmpty())
                    throw new IllegalStateException("Token is not set.");

                this.getTokenStore().fromEnv();
            }
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Version getVersion()
    {
        return Version.of("0.0.0");
    }

    @Override
    public KPMEnvironment getEnvironment()
    {
        return this.environment;
    }

    @Override
    public AliasProvider getAliasProvider()
    {
        return null;
    }

    @Override
    public PluginMetaManager getPluginMetaManager()
    {
        return new PluginMetaManagerMock();
    }

    @Override
    public KPMInfoManager getKpmInfoManager()
    {
        return new KPMInfoManagerMock();
    }

    @Override
    public HookExecutor getHookExecutor()
    {
        return new HookExecutorMock(this);
    }

    @Override
    public InstallManager getInstallManager()
    {
        return null;
    }

    @Override
    public PluginLoader getPluginLoader()
    {
        return this.loader;
    }

    @Override
    public ServerConditionChecker getServerConditionChecker()
    {
        return null;
    }

    @Override
    public void shutdown()
    {

    }
}
