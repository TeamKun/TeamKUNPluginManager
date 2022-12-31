package net.kunmc.lab.kpm;

import lombok.Getter;
import net.kunmc.lab.kpm.alias.AliasProviderImpl;
import net.kunmc.lab.kpm.hook.HookExecutorImpl;
import net.kunmc.lab.kpm.installer.InstallManagerImpl;
import net.kunmc.lab.kpm.interfaces.alias.AliasProvider;
import net.kunmc.lab.kpm.interfaces.hook.HookExecutor;
import net.kunmc.lab.kpm.interfaces.installer.InstallManager;
import net.kunmc.lab.kpm.interfaces.installer.loader.PluginLoader;
import net.kunmc.lab.kpm.interfaces.kpminfo.KPMInfoManager;
import net.kunmc.lab.kpm.interfaces.meta.PluginMetaManager;
import net.kunmc.lab.kpm.interfaces.resolver.PluginResolver;
import net.kunmc.lab.kpm.kpminfo.KPMInfoManagerImpl;
import net.kunmc.lab.kpm.meta.PluginMetaManagerImpl;
import net.kunmc.lab.kpm.resolver.PluginResolverImpl;
import net.kunmc.lab.kpm.task.PluginLoaderImpl;
import net.kunmc.lab.kpm.utils.ServerConditionChecker;
import net.kunmc.lab.kpm.versioning.Version;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

@Getter
class KPMRegistryImpl implements KPMRegistry
{
    private final Logger logger;
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

    public KPMRegistryImpl(@NotNull KPMEnvironment env)
    {
        this.logger = env.getLogger();
        this.environment = env;
        this.pluginMetaManager = new PluginMetaManagerImpl(
                this,
                env.getMetadataDBPath(),
                env.getPlugin()
        );
        this.aliasProvider = new AliasProviderImpl(env.getAliasesDBPath());
        this.kpmInfoManager = new KPMInfoManagerImpl(this);
        this.hookExecutor = new HookExecutorImpl(this);
        this.tokenStore = new TokenStore(env.getTokenPath(), env.getTokenKeyPath());
        this.installManager = new InstallManagerImpl(this.tokenStore);
        this.pluginLoader = new PluginLoaderImpl(this);
        this.pluginResolver = new PluginResolverImpl();
        this.serverConditionChecker = new ServerConditionChecker();
    }

    public Version getVersion()
    {
        return Version.of(this.getEnvironment().getPlugin().getDescription().getVersion());
    }
}
