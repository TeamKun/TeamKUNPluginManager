package org.kunlab.kpm.resolver.interfaces.result;

import org.kunlab.kpm.resolver.interfaces.BaseResolver;

/**
 * プラグインが一意に決定できなかった場合に返される結果です。
 */
public interface MultiResult extends ResolveResult
{
    BaseResolver getResolver();

    ResolveResult[] getResults();
}
