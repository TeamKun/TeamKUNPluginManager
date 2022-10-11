package net.kunmc.lab.teamkunpluginmanager.resolver.result;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.BaseResolver;

/**
 * 解決結果が複数ある場合に返されるクラス。
 */
@Value
public class MultiResult implements ResolveResult
{
    /**
     * リゾルバ
     */
    BaseResolver resolver;
    /**
     * The results.
     */
    ResolveResult[] results;
}
