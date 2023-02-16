package org.kunlab.kpm.interfaces.resolver.result;

import org.kunlab.kpm.interfaces.resolver.BaseResolver;

/**
 * プラグインが一意に決定できなかった場合に返される結果です。
 */
public interface MultiResult extends ResolveResult
{
    BaseResolver getResolver();

    ResolveResult[] getResults();
}
