package net.kunmc.lab.teamkunpluginmanager.resolver.result;

import lombok.Value;

/**
 * 解決結果が複数ある場合に返されるクラス。
 */
@Value
public class MultiResult implements ResolveResult
{
    /**
     * The results.
     */
    ResolveResult[] results;
}
