package net.kunmc.lab.kpm.interfaces.resolver.result;

import net.kunmc.lab.kpm.interfaces.resolver.BaseResolver;

/**
 * プラグインが一意に決定できなかった場合に返される結果です。
 */
public interface MultiResult extends ResolveResult
{
    BaseResolver getResolver();

    ResolveResult[] getResults();
}
