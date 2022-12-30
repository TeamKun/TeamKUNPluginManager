package net.kunmc.lab.kpm;

import lombok.Getter;
import net.kunmc.lab.kpm.alias.AliasProviderImpl;
import net.kunmc.lab.kpm.interfaces.alias.AliasProvider;
import net.kunmc.lab.kpm.interfaces.meta.PluginMetaManager;
import net.kunmc.lab.kpm.meta.PluginMetaManagerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

@Getter
class KPMRegistryImpl implements KPMRegistry
{
    private final Logger logger;
    private final AliasProvider aliasProvider;
    private final PluginMetaManager pluginMetaManager;

    public KPMRegistryImpl(@NotNull KPMEnvironment env)
    {
        this.logger = env.getLogger();
        this.pluginMetaManager = new PluginMetaManagerImpl(
                this,
                env.getMetadataDBPath(),
                env.getPlugin()
        );
        this.aliasProvider = new AliasProviderImpl(env.getAliasesDBPath());
    }
}
