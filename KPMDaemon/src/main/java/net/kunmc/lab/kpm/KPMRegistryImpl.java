package net.kunmc.lab.kpm;

import lombok.Getter;
import net.kunmc.lab.kpm.alias.AliasProviderImpl;
import net.kunmc.lab.kpm.hook.HookExecutorImpl;
import net.kunmc.lab.kpm.interfaces.alias.AliasProvider;
import net.kunmc.lab.kpm.interfaces.hook.HookExecutor;
import net.kunmc.lab.kpm.interfaces.kpminfo.KPMInfoManager;
import net.kunmc.lab.kpm.interfaces.meta.PluginMetaManager;
import net.kunmc.lab.kpm.kpminfo.KPMInfoManagerImpl;
import net.kunmc.lab.kpm.meta.PluginMetaManagerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

@Getter
class KPMRegistryImpl implements KPMRegistry
{
    private final Logger logger;
    private final AliasProvider aliasProvider;
    private final PluginMetaManager pluginMetaManager;
    private final KPMInfoManager kpmInfoManager;
    private final HookExecutor hookExecutor;

    public KPMRegistryImpl(@NotNull KPMEnvironment env)
    {
        this.logger = env.getLogger();
        this.pluginMetaManager = new PluginMetaManagerImpl(
                this,
                env.getMetadataDBPath(),
                env.getPlugin()
        );
        this.aliasProvider = new AliasProviderImpl(env.getAliasesDBPath());
        this.kpmInfoManager = new KPMInfoManagerImpl(this);
        this.hookExecutor = new HookExecutorImpl(this);
    }
}
