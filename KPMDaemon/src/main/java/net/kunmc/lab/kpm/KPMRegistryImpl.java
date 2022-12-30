package net.kunmc.lab.kpm;

import lombok.Getter;
import net.kunmc.lab.kpm.alias.AliasProviderImpl;
import net.kunmc.lab.kpm.interfaces.alias.AliasProvider;
import org.jetbrains.annotations.NotNull;

@Getter
class KPMRegistryImpl implements KPMRegistry
{
    private final AliasProvider aliasProvider;

    public KPMRegistryImpl(@NotNull KPMEnvironment env)
    {
        this.aliasProvider = new AliasProviderImpl(env.getAliasesDBPath());
    }
}
